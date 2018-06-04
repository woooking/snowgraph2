package edu.pku.sei.tsr.snowgraph.jiraextractor;

import com.google.common.collect.ImmutableMap;
import edu.pku.sei.tsr.snowgraph.jiraextractor.entity.IssueCommentInfo;
import edu.pku.sei.tsr.snowgraph.jiraextractor.entity.IssueInfo;
import edu.pku.sei.tsr.snowgraph.jiraextractor.entity.IssueUserInfo;
import edu.pku.sei.tsr.snowgraph.jiraextractor.entity.PatchInfo;

import java.util.Map;

class JiraUtils {

    public static Map<String, Object> buildPropertyMap(IssueInfo issueInfo) {
        return ImmutableMap.<String, Object>builder()
            .put(JiraGraphBuilder.ISSUE_ID, issueInfo.getIssueId())
            .put(JiraGraphBuilder.ISSUE_NAME, issueInfo.getIssueName())
            .put(JiraGraphBuilder.ISSUE_SUMMARY, issueInfo.getSummary())
            .put(JiraGraphBuilder.ISSUE_TYPE, issueInfo.getType())
            .put(JiraGraphBuilder.ISSUE_STATUS, issueInfo.getStatus())
            .put(JiraGraphBuilder.ISSUE_PRIORITY, issueInfo.getPriority())
            .put(JiraGraphBuilder.ISSUE_RESOLUTION, issueInfo.getResolution())
            .put(JiraGraphBuilder.ISSUE_VERSIONS, issueInfo.getVersions())
            .put(JiraGraphBuilder.ISSUE_FIX_VERSIONS, issueInfo.getFixVersions())
            .put(JiraGraphBuilder.ISSUE_COMPONENTS, issueInfo.getComponents())
            .put(JiraGraphBuilder.ISSUE_LABELS, issueInfo.getLabels())
            .put(JiraGraphBuilder.ISSUE_DESCRIPTION, issueInfo.getDescription())
            .put(JiraGraphBuilder.ISSUE_CREATOR_NAME, issueInfo.getCrearorName())
            .put(JiraGraphBuilder.ISSUE_ASSIGNEE_NAME, issueInfo.getAssigneeName())
            .put(JiraGraphBuilder.ISSUE_REPORTER_NAME, issueInfo.getReporterName())
            .put(JiraGraphBuilder.ISSUE_CREATED_DATE, issueInfo.getCreatedDate())
            .put(JiraGraphBuilder.ISSUE_UPDATED_DATE, issueInfo.getUpdatedDate())
            .put(JiraGraphBuilder.ISSUE_RESOLUTION_DATE, issueInfo.getResolutionDate())
            .build();
    }

    public static Map<String, Object> buildPropertyMap(PatchInfo patchInfo) {
        return ImmutableMap.<String, Object>builder()
            .put(JiraGraphBuilder.PATCH_ISSUE_ID, patchInfo.getIssueId())
            .put(JiraGraphBuilder.PATCH_ID, patchInfo.getPatchId())
            .put(JiraGraphBuilder.PATCH_NAME, patchInfo.getPatchName())
            .put(JiraGraphBuilder.PATCH_CONTENT, patchInfo.getContent())
            .put(JiraGraphBuilder.PATCH_CREATOR_NAME, patchInfo.getCreatorName())
            .put(JiraGraphBuilder.PATCH_CREATED_DATE, patchInfo.getCreatedDate())
            .build();
    }

    public static Map<String, Object> buildPropertyMap(IssueCommentInfo issueCommentInfo) {
        return ImmutableMap.<String, Object>builder()
            .put(JiraGraphBuilder.ISSUECOMMENT_ID, issueCommentInfo.getCommentId())
            .put(JiraGraphBuilder.ISSUECOMMENT_BODY, issueCommentInfo.getBody())
            .put(JiraGraphBuilder.ISSUECOMMENT_CREATOR_NAME, issueCommentInfo.getCreatorName())
            .put(JiraGraphBuilder.ISSUECOMMENT_UPDATER_NAME, issueCommentInfo.getUpdaterName())
            .put(JiraGraphBuilder.ISSUECOMMENT_CREATED_DATE, issueCommentInfo.getCreatedDate())
            .put(JiraGraphBuilder.ISSUECOMMENT_UPDATED_DATE, issueCommentInfo.getUpdatedDate())
            .build();
    }

    public static Map<String, Object> buildPropertyMap(IssueUserInfo issueUserInfo) {
        return ImmutableMap.<String, Object>builder()
            .put(JiraGraphBuilder.ISSUEUSER_NAME, issueUserInfo.getName())
            .put(JiraGraphBuilder.ISSUEUSER_EMAIL_ADDRESS, issueUserInfo.getName())
            .put(JiraGraphBuilder.ISSUEUSER_DISPLAY_NAME, issueUserInfo.getName())
            .put(JiraGraphBuilder.ISSUEUSER_ACTIVE, issueUserInfo.getName())
            .build();
    }

}
