/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.service;

import org.gluu.oxauth.ciba.CIBAPingCallbackProxy;
import org.gluu.oxauth.ciba.CIBAPushErrorProxy;
import org.gluu.oxauth.model.ciba.PushErrorResponseType;
import org.gluu.oxauth.model.common.AuthorizationGrantList;
import org.gluu.oxauth.model.common.BackchannelTokenDeliveryMode;
import org.gluu.oxauth.model.common.CIBAGrant;
import org.gluu.oxauth.model.common.CIBAGrantUserAuthorization;
import org.gluu.oxauth.model.configuration.AppConfiguration;
import org.gluu.oxauth.model.ldap.CIBARequest;
import org.gluu.oxauth.util.ServerUtil;
import org.gluu.service.cdi.async.Asynchronous;
import org.gluu.service.cdi.event.CibaRequestsProcessorEvent;
import org.gluu.service.cdi.event.Scheduled;
import org.gluu.service.timer.event.TimerEvent;
import org.gluu.service.timer.schedule.TimerSchedule;
import org.slf4j.Logger;

import javax.ejb.DependsOn;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Job responsible to process all expired CIBA requests and update their status.
 *
 * @author Milton BO
 * @version May 20, 2020
 */
@ApplicationScoped
@DependsOn("appInitializer")
@Named
public class CibaRequestsProcessorJob {

    public static final int CHUNK_SIZE = 500; // Default value whether there isn't backchannelRequestsProcessorJobChunkSize json property value

    @Inject
    private Logger log;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private Event<TimerEvent> processorEvent;

    @Inject
    private CIBAPushErrorProxy cibaPushErrorProxy;

    @Inject
    private CIBAPingCallbackProxy cibaPingCallbackProxy;

    @Inject
    private AuthorizationGrantList authorizationGrantList;

    @Inject
    private CibaRequestService cibaRequestService;

    private long lastFinishedTime;

    private AtomicBoolean isActive;

    private ExecutorService executorService;

    /**
     * Method invoked from the appInitializer to start processing every some time.
     */
    public void initTimer() {
        log.debug("Initializing CIBA requests processor");
        this.isActive = new AtomicBoolean(false);
        int intervalSec = appConfiguration.getBackchannelRequestsProcessorJobIntervalSec();

        // Schedule to start processor every N seconds
        processorEvent.fire(new TimerEvent(new TimerSchedule(intervalSec, intervalSec),
                new CibaRequestsProcessorEvent(), Scheduled.Literal.INSTANCE));

        this.lastFinishedTime = System.currentTimeMillis();
        this.executorService = Executors.newCachedThreadPool(ServerUtil.daemonThreadFactory());
    }

    @Asynchronous
    public void process(@Observes @Scheduled CibaRequestsProcessorEvent cibaRequestsProcessorEvent) {
        if (this.isActive.get()) {
            return;
        }
        if (!this.isActive.compareAndSet(false, true)) {
            return;
        }

        try {
            if (jobIsFree()) {
                processImpl();
                this.lastFinishedTime = System.currentTimeMillis();
            } else {
                log.trace("Starting conditions aren't reached for CIBA requestes processor");
            }
        } finally {
            this.isActive.set(false);
        }
    }

    /**
     * Defines whether the job is still in process or it is free according to the time interval defined.
     * @return True in case it is free to start a new process.
     */
    private boolean jobIsFree() {
        int interval = appConfiguration.getBackchannelRequestsProcessorJobIntervalSec();
        if (interval < 0) {
            log.info("CIBA Requests processor timer is disabled.");
            log.warn("CIBA Requests processor timer Interval (cleanServiceInterval in oxauth configuration) is negative which turns OFF internal clean up by the server. Please set it to positive value if you wish internal CIBA Requests processor up timer run.");
            return false;
        }

        long timeDiffrence = System.currentTimeMillis() - this.lastFinishedTime;
        return timeDiffrence >= interval * 1000;
    }

    /**
     * Main process that process CIBA requests in cache.
     */
    public void processImpl() {
        try {
            int chunkSize = appConfiguration.getBackchannelRequestsProcessorJobChunkSize() <= 0 ?
                    CHUNK_SIZE : appConfiguration.getBackchannelRequestsProcessorJobChunkSize();

            List<CIBARequest> expiredRequests = cibaRequestService.loadExpiredByStatus(
                    CIBAGrantUserAuthorization.AUTHORIZATION_PENDING, chunkSize);
            expiredRequests.forEach(cibaRequest -> cibaRequestService.updateStatus(cibaRequest,
                    CIBAGrantUserAuthorization.AUTHORIZATION_IN_PROCESS));

            for (CIBARequest expiredRequest : expiredRequests) {
                CIBAGrant cibaGrant = authorizationGrantList.getCIBAGrant(expiredRequest.getAuthReqId());
                if (cibaGrant != null) {
                    executorService.execute(() ->
                        processExpiredRequest(cibaGrant, expiredRequest.getAuthReqId())
                    );
                }
                cibaRequestService.removeCibaRequest(expiredRequest);
            }
        } catch (Exception e) {
            log.error("Failed to process CIBA request from cache.", e);
        }
    }

    /**
     * Method responsible to process expired CIBA grants, set them as expired in cache
     * and send callbacks to the client
     * @param cibaGrant Object containing data related to the CIBA grant.
     * @param authReqId Authentication request id.
     */
    private void processExpiredRequest(CIBAGrant cibaGrant, String authReqId) {
        if (cibaGrant.getUserAuthorization() != CIBAGrantUserAuthorization.AUTHORIZATION_PENDING
                && cibaGrant.getUserAuthorization() != CIBAGrantUserAuthorization.AUTHORIZATION_EXPIRED) {
            return;
        }
        log.info("Authentication request id {} has expired", authReqId);

        cibaGrant.setUserAuthorization(CIBAGrantUserAuthorization.AUTHORIZATION_EXPIRED);
        cibaGrant.setTokensDelivered(false);
        cibaGrant.save();

        if (cibaGrant.getClient().getBackchannelTokenDeliveryMode() == BackchannelTokenDeliveryMode.PUSH) {
            cibaPushErrorProxy.pushError(cibaGrant.getCIBAAuthenticationRequestId().getCode(),
                    cibaGrant.getClient().getBackchannelClientNotificationEndpoint(),
                    cibaGrant.getClientNotificationToken(),
                    PushErrorResponseType.EXPIRED_TOKEN,
                    "Request has expired and there was no answer from the end user.");
        } else if (cibaGrant.getClient().getBackchannelTokenDeliveryMode() == BackchannelTokenDeliveryMode.PING) {
            cibaPingCallbackProxy.pingCallback(
                    cibaGrant.getCIBAAuthenticationRequestId().getCode(),
                    cibaGrant.getClient().getBackchannelClientNotificationEndpoint(),
                    cibaGrant.getClientNotificationToken()
            );
        }
    }

}