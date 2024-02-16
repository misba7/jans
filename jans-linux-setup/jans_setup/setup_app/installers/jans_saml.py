import os
import glob
import shutil
import time
import socket
import tempfile
import uuid

from setup_app import paths
from setup_app.utils import base
from setup_app.utils.package_utils import packageUtils
from setup_app.static import AppType, InstallOption
from setup_app.config import Config
from setup_app.installers.jetty import JettyInstaller
from setup_app.utils.ldif_utils import create_client_ldif

# Config
Config.idp_config_http_port = '8083'
Config.jans_idp_enabled = 'true'
Config.jans_idp_realm = 'jans'
Config.jans_idp_client_id = f'jans-{uuid.uuid4()}'
Config.jans_idp_client_secret = os.urandom(10).hex()
Config.jans_idp_grant_type = 'PASSWORD'
Config.jans_idp_user_name = 'jans'
Config.jans_idp_user_password = os.urandom(10).hex()
Config.jans_idp_idp_root_dir = os.path.join(Config.jansOptFolder, 'idp')
Config.jans_idp_ignore_validation = 'true'
Config.jans_idp_idp_metadata_file = 'idp-metadata.xml'

class JansSamlInstaller(JettyInstaller):

    install_var = 'install_jans_saml'

    source_files = [
        (os.path.join(Config.dist_jans_dir, 'kc-jans-storage-plugin.jar'), os.path.join(base.current_app.app_info['JANS_MAVEN'], 'maven/io/jans/kc-jans-storage-plugin/{0}/kc-jans-storage-plugin-{0}.jar').format(base.current_app.app_info['jans_version'])),
        (os.path.join(Config.dist_jans_dir, 'jans-scim-model.jar'), os.path.join(base.current_app.app_info['JANS_MAVEN'], 'maven/io/jans/jans-scim-model/{0}/jans-scim-model-{0}.jar').format(base.current_app.app_info['jans_version'])),
        (os.path.join(Config.dist_jans_dir, 'kc-jans-storage-plugin-deps.zip'), os.path.join(base.current_app.app_info['JANS_MAVEN'], 'maven/io/jans/kc-jans-storage-plugin/{0}/kc-jans-storage-plugin-{0}-deps.zip').format(base.current_app.app_info['jans_version'])),
        (os.path.join(Config.dist_app_dir, 'keycloak.zip'), 'https://github.com/keycloak/keycloak/releases/download/{0}/keycloak-{0}.zip'.format(base.current_app.app_info['KC_VERSION'])),
        (os.path.join(Config.dist_jans_dir, 'kc-jans-authn-plugin.jar'), os.path.join(base.current_app.app_info['JANS_MAVEN'], 'maven/io/jans/kc-jans-authn-plugin/{0}/kc-jans-authn-plugin-{0}.jar').format(base.current_app.app_info['jans_version'])),
        (os.path.join(Config.dist_jans_dir, 'kc-jans-authn-plugin-deps.zip'), os.path.join(base.current_app.app_info['JANS_MAVEN'], 'maven/io/jans/kc-jans-authn-plugin/{0}/kc-jans-authn-plugin-{0}-deps.zip').format(base.current_app.app_info['jans_version'])),
        (os.path.join(Config.dist_jans_dir, 'kc-saml-plugin.jar'), os.path.join(base.current_app.app_info['JANS_MAVEN'], 'maven/io/jans/jans-config-api/plugins/kc-saml-plugin/{0}/kc-saml-plugin-{0}-distribution.jar').format(base.current_app.app_info['jans_version'])),
            ]

    def __init__(self):
        setattr(base.current_app, self.__class__.__name__, self)
        self.service_name = 'jans-saml'
        self.needdb = True
        self.app_type = AppType.SERVICE
        self.install_type = InstallOption.OPTONAL
        self.systemd_units = ['kc']
        self.register_progess()

        self.saml_enabled = True
        self.config_generation = True
        self.ignore_validation = True
        self.idp_root_dir = os.path.join(Config.opt_dir, 'idp/configs/')

        # sample config
        self.idp_config_id = 'keycloak'
        self.idp_config_root_dir = os.path.join(self.idp_root_dir, self.idp_config_id)
        self.idp_config_enabled = 'true'

        self.idp_config_data_dir = os.path.join(Config.opt_dir, self.idp_config_id)
        self.idp_config_log_dir = os.path.join(self.idp_config_data_dir, 'logs')
        self.idp_config_providers_dir = os.path.join(self.idp_config_data_dir, 'providers')
        self.output_folder = os.path.join(Config.output_dir, self.service_name)
        self.templates_folder = os.path.join(Config.templateFolder, self.service_name)
        self.ldif_config_fn = os.path.join(self.output_folder, 'configuration.ldif')
        self.config_json_fn = os.path.join(self.templates_folder, 'jans-saml-config.json')
        self.idp_config_fn = os.path.join(self.templates_folder, 'keycloak.conf')
        self.clients_json_fn = os.path.join(self.templates_folder, 'clients.json')

        Config.jans_idp_idp_metadata_root_dir = os.path.join(self.idp_config_root_dir, 'idp/metadata')
        Config.jans_idp_idp_metadata_temp_dir = os.path.join(self.idp_config_root_dir, 'idp/temp_metadata')
        Config.jans_idp_sp_metadata_root_dir = os.path.join(self.idp_config_root_dir, 'sp/metadata')
        Config.jans_idp_sp_metadata_temp_dir = os.path.join(self.idp_config_root_dir, 'sp/temp_metadata')

        Config.idp_config_hostname = Config.hostname
        Config.keycloack_hostname = Config.hostname


    def install(self):
        """installation steps"""
        self.create_clients()
        self.install_keycloack()


    def render_import_templates(self):
        self.logIt("Preparing base64 encodings configuration files")

        self.renderTemplateInOut(self.config_json_fn, self.templates_folder, self.output_folder, pystring=True)
        Config.templateRenderingDict['saml_dynamic_conf_base64'] = self.generate_base64_ldap_file(
                os.path.join(self.output_folder,os.path.basename(self.config_json_fn))
            )
        self.renderTemplateInOut(self.ldif_config_fn, self.templates_folder, self.output_folder)

        self.dbUtils.import_ldif([self.ldif_config_fn])


    def create_folders(self):
        for saml_dir in (self.idp_root_dir, self.idp_config_root_dir, self.idp_config_data_dir,
                        self.idp_config_log_dir, self.idp_config_providers_dir,
                        Config.jans_idp_idp_metadata_temp_dir, Config.jans_idp_idp_metadata_root_dir,
                        Config.jans_idp_sp_metadata_root_dir, Config.jans_idp_sp_metadata_temp_dir,
                ):
            self.createDirs(saml_dir)

        self.chown(self.idp_root_dir, Config.jetty_user, Config.jetty_group, recursive=True)
        self.run([paths.cmd_chmod, '0760', saml_dir])

    def create_clients(self):
        clients_data =  base.readJsonFile(self.clients_json_fn)
        client_ldif_fns = []
        for client_info in clients_data:
                check_client = self.check_clients([(client_info['client_var'], client_info['client_prefix'])])
                if check_client.get(client_info['client_prefix']) == -1:
                    scopes = client_info['scopes_dns']
                    for scope_id in client_info['scopes_ids']:
                        scope_info = self.dbUtils.search('ou=scopes,o=jans', search_filter=f'(jansId=scope_id)')
                        if scope_info:
                            scopes.append(scope_info['dn'])
                    client_id = getattr(Config, client_info['client_var'])
                    client_ldif_fn = os.path.join(self.output_folder, f'clients-{client_id}.ldif')
                    client_ldif_fns.append(client_ldif_fn)
                    encoded_pw_var = '_'.join(client_info["client_var"].split('_')[:-1])+'_encoded_pw'
                    if client_info['redirect_uri']:
                        for i, redirect_uri in enumerate(client_info['redirect_uri']):
                            client_info['redirect_uri'][i] = self.fomatWithDict(redirect_uri, Config.__dict__)
                    create_client_ldif(
                        ldif_fn=client_ldif_fn,
                        client_id=client_id,
                        description=client_info['description'],
                        display_name=client_info['display_name'],
                        encoded_pw=getattr(Config, encoded_pw_var),
                        scopes=scopes,
                        redirect_uri=client_info['redirect_uri'] ,
                        grant_types=client_info['grant_types'],
                        authorization_methods=client_info['authorization_methods'],
                        application_type=client_info['application_type'],
                        response_types=client_info['response_types']
                        )
        self.dbUtils.import_ldif(client_ldif_fns)

    def install_keycloack(self):
        self.logIt("Installing KC", pbar=self.service_name)
        base.unpack_zip(self.source_files[3][0], self.idp_config_data_dir, with_par_dir=False)
        self.update_rendering_dict()
        self.renderTemplateInOut(self.idp_config_fn, self.templates_folder, os.path.join(self.idp_config_data_dir, 'conf'))
        self.chown(self.idp_config_data_dir, Config.jetty_user, Config.jetty_group, recursive=True)


    def service_post_setup(self):
        self.deploy_jans_keycloack_providers()
        self.config_api_idp_plugin_config()


    def deploy_jans_keycloack_providers(self):
        self.copyFile(self.source_files[0][0], self.idp_config_providers_dir)
        self.copyFile(self.source_files[1][0], self.idp_config_providers_dir)
        base.unpack_zip(self.source_files[2][0], self.idp_config_providers_dir)
        self.copyFile(self.source_files[4][0], self.idp_config_providers_dir)
        base.unpack_zip(self.source_files[5][0], self.idp_config_providers_dir)


    def config_api_idp_plugin_config(self):

        # deploy config-api plugin
        base.current_app.ConfigApiInstaller.source_files.append(self.source_files[6])
        base.current_app.ConfigApiInstaller.install_plugin('kc-saml-plugin')

        # Render templates
        self.update_rendering_dict()
        jans_api_tmp_dir = os.path.join(self.templates_folder, 'kc_jans_api')
        jans_api_output_dir = os.path.join(self.output_folder, 'kc_jans_api')

        jans_api_openid_client_fn = 'jans.api-openid-client.json'
        jans_api_realm_fn = 'jans.api-realm.json'
        jans_api_user_fn = 'jans.api-user.json'
        jans_browser_auth_flow_fn = 'jans.browser-auth-flow.json'
        jans_execution_config_jans_fn = 'jans.execution-config-jans.json'

        self.idp_config_fn = os.path.join(self.templates_folder, 'keycloak.conf')

        for tmp_fn in (jans_api_openid_client_fn, jans_api_realm_fn, jans_api_user_fn, jans_browser_auth_flow_fn):
            self.renderTemplateInOut(os.path.join(jans_api_tmp_dir, tmp_fn), jans_api_tmp_dir, jans_api_output_dir, pystring=True)

        self.logIt("Starting KC for config api idp plugin configurations")
        self.start()
        #wait a while for KC to start
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        for i in range(24):
            self.logIt("Wait 5 seconds to KC started")
            time.sleep(5)
            try:
                self.logIt("Connecting KC")
                s.connect(('localhost', int(Config.idp_config_http_port)))
                self.logIt("Successfully connected to KC")
                break
            except Exception:
                self.logIt("KC not ready")
        else:
            self.logIt("KC did not start in 120 seconds. Giving up configuration", errorLog=True, fatal=True)

        kcadm_cmd = '/opt/keycloak/bin/kcadm.sh'
        kcm_server_url = f'http://localhost:{Config.idp_config_http_port}/kc'
        env = {'JAVA_HOME': Config.jre_home}

        with tempfile.TemporaryDirectory() as tmp_dir:
            kc_tmp_config = os.path.join(tmp_dir, 'kcadm-jans.config')
            self.run([kcadm_cmd, 'config', 'credentials', '--server', kcm_server_url, '--realm', 'master', '--user', 'admin', '--password', 'admin', '--config', kc_tmp_config], env=env)

            self.run([kcadm_cmd, 'config', 'credentials', '--server', kcm_server_url, '--realm', 'master', '--user', 'admin', '--password', Config.admin_password, '--config', kc_tmp_config], env=env)

            # Change default password
            self.run([kcadm_cmd, 'set-password', '-r', 'master', '--username', 'admin', '--new-password',  Config.admin_password, '--config', kc_tmp_config], env=env)

            # create realm
            self.run([kcadm_cmd, 'create', 'realms', '-f', os.path.join(jans_api_output_dir, jans_api_realm_fn),'--config', kc_tmp_config], env=env)

            # create client
            self.run([kcadm_cmd, 'create', 'clients', '-r', Config.jans_idp_realm, '-f', os.path.join(jans_api_output_dir, jans_api_openid_client_fn), '--config', kc_tmp_config], env=env)

            # create user and change password
            self.run([kcadm_cmd, 'create', 'users', '-r', Config.jans_idp_realm, '-f', os.path.join(jans_api_output_dir, jans_api_user_fn),'--config', kc_tmp_config], env=env)
            self.run([kcadm_cmd, 'set-password', '-r', Config.jans_idp_realm, '--username', Config.jans_idp_user_name, '--new-password', Config.jans_idp_user_password, '--config', kc_tmp_config], env=env)

            # assign roles to jans-api-user
            self.run([kcadm_cmd, 'add-roles', '-r', Config.jans_idp_realm, '--uusername', Config.jans_idp_user_name, '--cclientid', 'realm-management', '--rolename', 'manage-identity-providers', '--rolename', 'view-identity-providers', '--rolename', 'view-identity-providers', '--config', kc_tmp_config], env=env)

            # Create authentication flow in the jans-api realm used for saml clients
            _, result = self.run([kcadm_cmd, 'create', 'authentication/flows', '-r',  Config.jans_idp_realm, '-f', os.path.join(jans_api_output_dir, jans_browser_auth_flow_fn), '--config', kc_tmp_config], env=env, get_stderr=True)
            Config.templateRenderingDict['jans_browser_auth_flow_id'] = result.strip().split()[-1].strip("'").strip('"')

            jans_execution_auth_cookie_fn = 'jans.execution-auth-cookie.json'
            jans_execution_auth_jans_fn = 'jans.execution-auth-jans.json'

            for tmp_fn in (jans_execution_auth_cookie_fn, jans_execution_auth_jans_fn):
                self.renderTemplateInOut(os.path.join(jans_api_tmp_dir, tmp_fn), jans_api_tmp_dir, jans_api_output_dir, pystring=True)

            # Add execution steps to the flow created in the jansapi realm
            self.run([kcadm_cmd, 'create', 'authentication/executions', '-r', Config.jans_idp_realm, '-f', os.path.join(jans_api_output_dir, jans_execution_auth_cookie_fn), '--config', kc_tmp_config], env=env)
            _, result = self.run([kcadm_cmd, 'create', 'authentication/executions', '-r', Config.jans_idp_realm, '-f', os.path.join(jans_api_output_dir, jans_execution_auth_jans_fn), '--config', kc_tmp_config], env=env, get_stderr=True)
            jans_execution_auth_jans_id = result.strip().split()[-1].strip("'").strip('"')
            self.renderTemplateInOut(os.path.join(jans_api_tmp_dir, jans_execution_config_jans_fn), jans_api_tmp_dir, jans_api_output_dir, pystring=True)

            # Configure the jans auth execution step in realm jans-api
            self.run([kcadm_cmd, 'create', f'authentication/executions/{jans_execution_auth_jans_id}/config', '-r', Config.jans_idp_realm, '-f', os.path.join(jans_api_output_dir, jans_execution_config_jans_fn), '--config', kc_tmp_config], env=env)


