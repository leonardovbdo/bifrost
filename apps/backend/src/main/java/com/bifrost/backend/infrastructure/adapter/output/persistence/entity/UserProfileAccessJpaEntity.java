package com.bifrost.backend.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_profile_access")
@IdClass(UserProfileAccessJpaEntity.Pk.class)
public class UserProfileAccessJpaEntity {
  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Id
  @Column(name = "robot_profile_id")
  private UUID robotProfileId;

  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  public UUID getRobotProfileId() { return robotProfileId; }
  public void setRobotProfileId(UUID robotProfileId) { this.robotProfileId = robotProfileId; }

  public static class Pk implements Serializable {
    private UUID userId;
    private UUID robotProfileId;

    public Pk() {}

    public Pk(UUID userId, UUID robotProfileId) {
      this.userId = userId;
      this.robotProfileId = robotProfileId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Pk pk)) return false;
      return Objects.equals(userId, pk.userId) && Objects.equals(robotProfileId, pk.robotProfileId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userId, robotProfileId);
    }
  }
}
