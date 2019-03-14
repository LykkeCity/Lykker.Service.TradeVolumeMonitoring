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
import com.lykke.trade.volume.monitoring.service.loader.http.generated.client.model.IVolumePrice;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.threeten.bp.OffsetDateTime;

/**
 * IOrderBook
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-03-14T05:26:29.590Z")
public class IOrderBook {
  @SerializedName("assetPair")
  private String assetPair = null;

  @SerializedName("isBuy")
  private Boolean isBuy = null;

  @SerializedName("timestamp")
  private OffsetDateTime timestamp = null;

  @SerializedName("prices")
  private List<IVolumePrice> prices = null;

   /**
   * Get assetPair
   * @return assetPair
  **/
  @ApiModelProperty(value = "")
  public String getAssetPair() {
    return assetPair;
  }

   /**
   * Get isBuy
   * @return isBuy
  **/
  @ApiModelProperty(value = "")
  public Boolean isIsBuy() {
    return isBuy;
  }

   /**
   * Get timestamp
   * @return timestamp
  **/
  @ApiModelProperty(value = "")
  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

   /**
   * Get prices
   * @return prices
  **/
  @ApiModelProperty(value = "")
  public List<IVolumePrice> getPrices() {
    return prices;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IOrderBook iorderBook = (IOrderBook) o;
    return Objects.equals(this.assetPair, iorderBook.assetPair) &&
        Objects.equals(this.isBuy, iorderBook.isBuy) &&
        Objects.equals(this.timestamp, iorderBook.timestamp) &&
        Objects.equals(this.prices, iorderBook.prices);
  }

  @Override
  public int hashCode() {
    return Objects.hash(assetPair, isBuy, timestamp, prices);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IOrderBook {\n");
    
    sb.append("    assetPair: ").append(toIndentedString(assetPair)).append("\n");
    sb.append("    isBuy: ").append(toIndentedString(isBuy)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    prices: ").append(toIndentedString(prices)).append("\n");
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

