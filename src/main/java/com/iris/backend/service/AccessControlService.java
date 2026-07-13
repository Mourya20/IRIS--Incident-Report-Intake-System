package com.iris.backend.service;

import com.iris.backend.entity.AppUser;
import com.iris.backend.entity.Incident;
import com.iris.backend.enums.IncidentStatus;
import com.iris.backend.enums.RoleName;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
public class AccessControlService {

    private static final Set<IncidentStatus> OPEN_STATUSES = EnumSet.of(IncidentStatus.NEW, IncidentStatus.TRIAGED, IncidentStatus.IN_PROGRESS);

    public boolean hasRole(AppUser user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == roleName);
    }

    public void canViewIncident(AppUser user, Incident incident) {
        if (hasRole(user, RoleName.ADMIN)) {
            return;
        }
        if (hasRole(user, RoleName.REPORTER) && incident.getReporter().getId().equals(user.getId())) {
            return;
        }
        if (hasRole(user, RoleName.ANALYST)) {
            if ((incident.getAssignee() != null && incident.getAssignee().getId().equals(user.getId())) || OPEN_STATUSES.contains(incident.getStatus())) {
                return;
            }
        }
        throw new AccessDeniedException("Not authorized to view this incident");
    }

    public void canUpdateIncidentStatus(AppUser user, Incident incident) {
        if (hasRole(user, RoleName.ADMIN)) {
            return;
        }
        if (hasRole(user, RoleName.ANALYST)) {
            if ((incident.getAssignee() != null && incident.getAssignee().getId().equals(user.getId())) || OPEN_STATUSES.contains(incident.getStatus())) {
                return;
            }
        }
        throw new AccessDeniedException("Not authorized to update status");
    }

    public void canAddIoc(AppUser user, Incident incident) {
        if (hasRole(user, RoleName.ADMIN)) {
            return;
        }
        if (hasRole(user, RoleName.ANALYST)) {
            if ((incident.getAssignee() != null && incident.getAssignee().getId().equals(user.getId())) || OPEN_STATUSES.contains(incident.getStatus())) {
                return;
            }
        }
        throw new AccessDeniedException("Not authorized to add IOCs");
    }
}
