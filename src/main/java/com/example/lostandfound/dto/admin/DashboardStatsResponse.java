package com.example.lostandfound.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalUsers;
    private long totalLostReports;
    private long totalFoundReports;
    private long resolvedLostCases;
    private long returnedFoundCases;
}
