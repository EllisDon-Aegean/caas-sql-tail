package com.ellisdon.caas.sqltail.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RowValueDetails {

    @JsonProperty("type")
    private String type;

    @JsonProperty("value")
    private String value;
}
