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
import com.lykke.trade.volume.monitoring.service.loader.http.generated.client.model.ApiCandle2;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.threeten.bp.OffsetDateTime;

/**
 * CandlesHistoryResponseApiCandle2
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-03-14T05:26:29.590Z")
public class CandlesHistoryResponseApiCandle2 {
  @SerializedName("assetPair")
  private String assetPair = null;

  /**
   * Gets or Sets period
   */
  @JsonAdapter(PeriodEnum.Adapter.class)
  public enum PeriodEnum {
    SEC("Sec"),
    
    MINUTE("Minute"),
    
    MIN5("Min5"),
    
    MIN15("Min15"),
    
    MIN30("Min30"),
    
    HOUR("Hour"),
    
    HOUR4("Hour4"),
    
    HOUR6("Hour6"),
    
    HOUR12("Hour12"),
    
    DAY("Day"),
    
    WEEK("Week"),
    
    MONTH("Month");

    private String value;

    PeriodEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static PeriodEnum fromValue(String text) {
      for (PeriodEnum b : PeriodEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public static class Adapter extends TypeAdapter<PeriodEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final PeriodEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public PeriodEnum read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return PeriodEnum.fromValue(String.valueOf(value));
      }
    }
  }

  @SerializedName("period")
  private PeriodEnum period = null;

  @SerializedName("dateFrom")
  private OffsetDateTime dateFrom = null;

  @SerializedName("dateTo")
  private OffsetDateTime dateTo = null;

  /**
   * Gets or Sets type
   */
  @JsonAdapter(TypeEnum.Adapter.class)
  public enum TypeEnum {
    BID("Bid"),
    
    ASK("Ask"),
    
    MID("Mid"),
    
    TRADES("Trades");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public static class Adapter extends TypeAdapter<TypeEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final TypeEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public TypeEnum read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return TypeEnum.fromValue(String.valueOf(value));
      }
    }
  }

  @SerializedName("type")
  private TypeEnum type = null;

  @SerializedName("data")
  private List<ApiCandle2> data = null;

  public CandlesHistoryResponseApiCandle2 assetPair(String assetPair) {
    this.assetPair = assetPair;
    return this;
  }

   /**
   * Get assetPair
   * @return assetPair
  **/
  @ApiModelProperty(value = "")
  public String getAssetPair() {
    return assetPair;
  }

  public void setAssetPair(String assetPair) {
    this.assetPair = assetPair;
  }

  public CandlesHistoryResponseApiCandle2 period(PeriodEnum period) {
    this.period = period;
    return this;
  }

   /**
   * Get period
   * @return period
  **/
  @ApiModelProperty(value = "")
  public PeriodEnum getPeriod() {
    return period;
  }

  public void setPeriod(PeriodEnum period) {
    this.period = period;
  }

  public CandlesHistoryResponseApiCandle2 dateFrom(OffsetDateTime dateFrom) {
    this.dateFrom = dateFrom;
    return this;
  }

   /**
   * Get dateFrom
   * @return dateFrom
  **/
  @ApiModelProperty(value = "")
  public OffsetDateTime getDateFrom() {
    return dateFrom;
  }

  public void setDateFrom(OffsetDateTime dateFrom) {
    this.dateFrom = dateFrom;
  }

  public CandlesHistoryResponseApiCandle2 dateTo(OffsetDateTime dateTo) {
    this.dateTo = dateTo;
    return this;
  }

   /**
   * Get dateTo
   * @return dateTo
  **/
  @ApiModelProperty(value = "")
  public OffsetDateTime getDateTo() {
    return dateTo;
  }

  public void setDateTo(OffsetDateTime dateTo) {
    this.dateTo = dateTo;
  }

  public CandlesHistoryResponseApiCandle2 type(TypeEnum type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(value = "")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public CandlesHistoryResponseApiCandle2 data(List<ApiCandle2> data) {
    this.data = data;
    return this;
  }

  public CandlesHistoryResponseApiCandle2 addDataItem(ApiCandle2 dataItem) {
    if (this.data == null) {
      this.data = new ArrayList<ApiCandle2>();
    }
    this.data.add(dataItem);
    return this;
  }

   /**
   * Get data
   * @return data
  **/
  @ApiModelProperty(value = "")
  public List<ApiCandle2> getData() {
    return data;
  }

  public void setData(List<ApiCandle2> data) {
    this.data = data;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CandlesHistoryResponseApiCandle2 candlesHistoryResponseApiCandle2 = (CandlesHistoryResponseApiCandle2) o;
    return Objects.equals(this.assetPair, candlesHistoryResponseApiCandle2.assetPair) &&
        Objects.equals(this.period, candlesHistoryResponseApiCandle2.period) &&
        Objects.equals(this.dateFrom, candlesHistoryResponseApiCandle2.dateFrom) &&
        Objects.equals(this.dateTo, candlesHistoryResponseApiCandle2.dateTo) &&
        Objects.equals(this.type, candlesHistoryResponseApiCandle2.type) &&
        Objects.equals(this.data, candlesHistoryResponseApiCandle2.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(assetPair, period, dateFrom, dateTo, type, data);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CandlesHistoryResponseApiCandle2 {\n");
    
    sb.append("    assetPair: ").append(toIndentedString(assetPair)).append("\n");
    sb.append("    period: ").append(toIndentedString(period)).append("\n");
    sb.append("    dateFrom: ").append(toIndentedString(dateFrom)).append("\n");
    sb.append("    dateTo: ").append(toIndentedString(dateTo)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
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

