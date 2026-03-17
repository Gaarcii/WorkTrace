package com.worktrace.worktracebackend.dto.ip;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class IpResponseDto {
    private SecurityData security;
    private LocationData location;

    @Getter
    @Setter
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class SecurityData {

        @JsonProperty("is_vpn")
        private boolean vpn;

        @JsonProperty("is_proxy")
        private boolean proxy;

        @JsonProperty("is_tor")
        private boolean tor;

        @JsonProperty("is_relay")
        private boolean relay;

        @JsonProperty("is_abuse")
        private boolean abuse;
    }

    @Getter
    @Setter
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class LocationData {
        private String city;
        private String region;
        private String country;
        private Float longitude;
        private Float latitude;
    }
}