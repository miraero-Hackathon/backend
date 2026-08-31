package org.jejuro.miraero.domain.goal.milestone.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.domain.MilestoneReport;

import java.util.List;

@Mapper
public interface MilestoneMapper {

    List<Milestone> findByGoalId(
            @Param("goalId") Long goalId);

    Milestone findById(
            @Param("milestoneId") Long milestoneId);

    Milestone findPreviousMilestone(
            @Param("goalId") Long goalId,
            @Param("percentage") Integer percentage
    );


    Milestone findByGoalIdAndPercentage(
            @Param("goalId") Long goalId,
            @Param("percentage") Integer percentage
    );

    void save(
            @Param("milestone") Milestone milestone);

    void saveAll(
            @Param("milestones") List<Milestone> milestones);

    void deleteByGoalId(
            @Param("goalId") Long goalId);

    int updateAchievement(
            @Param("milestone") Milestone milestone);
}