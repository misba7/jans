/*
 * jans-api-server
 * jans-api-server
 *
 * OpenAPI spec version: 4.2
 * Contact: yuriyz@gluu.org
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * GetRequestObjectUriParams
 */


public class GetRequestObjectUriParams {
  @SerializedName("rp_id")
  private String rpId = null;

  @SerializedName("params")
  private Map<String, Object> params = null;

  @SerializedName("request_object_signing_alg")
  private String requestObjectSigningAlg = null;

  @SerializedName("rp_host_url")
  private String rpHostUrl = null;

  public GetRequestObjectUriParams rpId(String rpId) {
    this.rpId = rpId;
    return this;
  }

   /**
   * Get rpId
   * @return rpId
  **/
  @Schema(example = "bcad760f-91ba-46e1-a020-05e4281d91b6", required = true, description = "")
  public String getRpId() {
    return rpId;
  }

  public void setRpId(String rpId) {
    this.rpId = rpId;
  }

  public GetRequestObjectUriParams params(Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public GetRequestObjectUriParams putParamsItem(String key, Object paramsItem) {
    if (this.params == null) {
      this.params = new HashMap<String, Object>();
    }
    this.params.put(key, paramsItem);
    return this;
  }

   /**
   * Get params
   * @return params
  **/
  @Schema(description = "")
  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  public GetRequestObjectUriParams requestObjectSigningAlg(String requestObjectSigningAlg) {
    this.requestObjectSigningAlg = requestObjectSigningAlg;
    return this;
  }

   /**
   * choose the JWS alg algorithm (JWA) that must be required by the Authorization Server. Valid values are none, HS256, HS384, HS512, RS256, RS384, RS512, ES256, ES384, ES512, PS256, PS384, PS512
   * @return requestObjectSigningAlg
  **/
  @Schema(example = "RS256", description = "choose the JWS alg algorithm (JWA) that must be required by the Authorization Server. Valid values are none, HS256, HS384, HS512, RS256, RS384, RS512, ES256, ES384, ES512, PS256, PS384, PS512")
  public String getRequestObjectSigningAlg() {
    return requestObjectSigningAlg;
  }

  public void setRequestObjectSigningAlg(String requestObjectSigningAlg) {
    this.requestObjectSigningAlg = requestObjectSigningAlg;
  }

  public GetRequestObjectUriParams rpHostUrl(String rpHostUrl) {
    this.rpHostUrl = rpHostUrl;
    return this;
  }

   /**
   * Get rpHostUrl
   * @return rpHostUrl
  **/
  @Schema(example = "https://<rp-host>", required = true, description = "")
  public String getRpHostUrl() {
    return rpHostUrl;
  }

  public void setRpHostUrl(String rpHostUrl) {
    this.rpHostUrl = rpHostUrl;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetRequestObjectUriParams getRequestObjectUriParams = (GetRequestObjectUriParams) o;
    return Objects.equals(this.rpId, getRequestObjectUriParams.rpId) &&
        Objects.equals(this.params, getRequestObjectUriParams.params) &&
        Objects.equals(this.requestObjectSigningAlg, getRequestObjectUriParams.requestObjectSigningAlg) &&
        Objects.equals(this.rpHostUrl, getRequestObjectUriParams.rpHostUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rpId, params, requestObjectSigningAlg, rpHostUrl);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetRequestObjectUriParams {\n");
    
    sb.append("    rpId: ").append(toIndentedString(rpId)).append("\n");
    sb.append("    params: ").append(toIndentedString(params)).append("\n");
    sb.append("    requestObjectSigningAlg: ").append(toIndentedString(requestObjectSigningAlg)).append("\n");
    sb.append("    rpHostUrl: ").append(toIndentedString(rpHostUrl)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
