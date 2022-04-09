package study.datajpa.entity;

public interface NestedClosedMemberProjection {
    Long getId();
    String getUsername();
    TeamInfo getTeam();

    interface TeamInfo {
        String getName();
    }
}
