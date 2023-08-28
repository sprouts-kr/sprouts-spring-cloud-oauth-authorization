package kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class MemberRemoteResponse {
    private UUID id;
    private String email;
    private String name;
    private Boolean passwordExpired;
    private String passwordExpireDate;
    private String status;
    private String description;

    private LocalDateTime createdOn;
    private UUID createdBy;
    private LocalDateTime lastModifiedOn;
    private UUID lastModifiedBy;
}
