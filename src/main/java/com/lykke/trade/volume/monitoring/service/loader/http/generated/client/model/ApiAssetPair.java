/*
 * 
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.lykke.trade.volume.monitoring.service.loader.http.generated.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * ApiAssetPair
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-03-14T05:26:29.590Z")
public class ApiAssetPair {
  @SerializedName("id")
  private String id = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("accuracy")
  private Integer accuracy = null;

  @SerializedName("invertedAccuracy")
  private Integer invertedAccuracy = null;

  @SerializedName("baseAssetId")
  private String baseAssetId = null;

  @SerializedName("quotingAssetId")
  private String quotingAssetId = null;

  public ApiAssetPair id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ApiAssetPair name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ApiAssetPair accuracy(Integer accuracy) {
    this.accuracy = accuracy;
    return this;
  }

   /**
   * Get accuracy
   * @return accuracy
  **/
  @ApiModelProperty(value = "")
  public Integer getAccuracy() {
    return accuracy;
  }

  public void setAccuracy(Integer accuracy) {
    this.accuracy = accuracy;
  }

  public ApiAssetPair invertedAccuracy(Integer invertedAccuracy) {
    this.invertedAccuracy = invertedAccuracy;
    return this;
  }

   /**
   * Get invertedAccuracy
   * @return invertedAccuracy
  **/
  @ApiModelProperty(value = "")
  public Integer getInvertedAccuracy() {
    return invertedAccuracy;
  }

  public void setInvertedAccuracy(Integer invertedAccuracy) {
    this.invertedAccuracy = invertedAccuracy;
  }

  public ApiAssetPair baseAssetId(String baseAssetId) {
    this.baseAssetId = baseAssetId;
    return this;
  }

   /**
   * Get baseAssetId
   * @return baseAssetId
  **/
  @ApiModelProperty(value = "")
  public String getBaseAssetId() {
    return baseAssetId;
  }

  public void setBaseAssetId(String baseAssetId) {
    this.baseAssetId = baseAssetId;
  }

  public ApiAssetPair quotingAssetId(String quotingAssetId) {
    this.quotingAssetId = quotingAssetId;
    return this;
  }

   /**
   * Get quotingAssetId
   * @return quotingAssetId
  **/
  @ApiModelProperty(value = "")
  public String getQuotingAssetId() {
    return quotingAssetId;
  }

  public void setQuotingAssetId(String quotingAssetId) {
    this.quotingAssetId = quotingAssetId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiAssetPair apiAssetPair = (ApiAssetPair) o;
    return Objects.equals(this.id, apiAssetPair.id) &&
        Objects.equals(this.name, apiAssetPair.name) &&
        Objects.equals(this.accuracy, apiAssetPair.accuracy) &&
        Objects.equals(this.invertedAccuracy, apiAssetPair.invertedAccuracy) &&
        Objects.equals(this.baseAssetId, apiAssetPair.baseAssetId) &&
        Objects.equals(this.quotingAssetId, apiAssetPair.quotingAssetId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, accuracy, invertedAccuracy, baseAssetId, quotingAssetId);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiAssetPair {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    accuracy: ").append(toIndentedString(accuracy)).append("\n");
    sb.append("    invertedAccuracy: ").append(toIndentedString(invertedAccuracy)).append("\n");
    sb.append("    baseAssetId: ").append(toIndentedString(baseAssetId)).append("\n");
    sb.append("    quotingAssetId: ").append(toIndentedString(quotingAssetId)).append("\n");
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
