package yokwe.stock.jp.xbrl.inline;

public enum Context {
    ANNUAL_MEMBER                    ("AnnualMember"),
    CONSOLIDATED_MEMBER              ("ConsolidatedMember"),
    CURRENT_ACCUMULATED_Q_1_DURATION ("CurrentAccumulatedQ1Duration"),
    CURRENT_ACCUMULATED_Q_1_INSTANT  ("CurrentAccumulatedQ1Instant"),
    CURRENT_ACCUMULATED_Q_2_DURATION ("CurrentAccumulatedQ2Duration"),
    CURRENT_ACCUMULATED_Q_2_INSTANT  ("CurrentAccumulatedQ2Instant"),
    CURRENT_ACCUMULATED_Q_3_DURATION ("CurrentAccumulatedQ3Duration"),
    CURRENT_ACCUMULATED_Q_3_INSTANT  ("CurrentAccumulatedQ3Instant"),
    CURRENT_MEMBER                   ("CurrentMember"),
    CURRENT_YEAR_DURATION            ("CurrentYearDuration"),
    CURRENT_YEAR_INSTANT             ("CurrentYearInstant"),
    FIRST_QUARTER_MEMBER             ("FirstQuarterMember"),
    FORECAST_MEMBER                  ("ForecastMember"),
    LOWER_MEMBER                     ("LowerMember"),
    NEXT_2_YEAR_DURATION             ("Next2YearDuration"),
    NEXT_ACCUMULATED_Q_1_DURATION    ("NextAccumulatedQ1Duration"),
    NEXT_ACCUMULATED_Q_2_DURATION    ("NextAccumulatedQ2Duration"),
    NEXT_ACCUMULATED_Q_3_DURATION    ("NextAccumulatedQ3Duration"),
    NEXT_YEAR_DURATION               ("NextYearDuration"),
    NON_CONSOLIDATED_MEMBER          ("NonConsolidatedMember"),
    PREVIOUS_MEMBER                  ("PreviousMember"),
    PRIOR_ACCUMULATED_Q_1_DURATION   ("PriorAccumulatedQ1Duration"),
    PRIOR_ACCUMULATED_Q_2_DURATION   ("PriorAccumulatedQ2Duration"),
    PRIOR_ACCUMULATED_Q_2_INSTANT    ("PriorAccumulatedQ2Instant"),
    PRIOR_ACCUMULATED_Q_3_DURATION   ("PriorAccumulatedQ3Duration"),
    PRIOR_ACCUMULATED_Q_3_INSTANT    ("PriorAccumulatedQ3Instant"),
    PRIOR_YEAR_DURATION              ("PriorYearDuration"),
    PRIOR_YEAR_INSTANT               ("PriorYearInstant"),
    RESULT_MEMBER                    ("ResultMember"),
    SECOND_QUARTER_MEMBER            ("SecondQuarterMember"),
    THIRD_QUARTER_MEMBER             ("ThirdQuarterMember"),
    UPPER_MEMBER                     ("UpperMember"),
    YEAR_END_MEMBER                  ("YearEndMember");

    public final String value;

    Context(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
