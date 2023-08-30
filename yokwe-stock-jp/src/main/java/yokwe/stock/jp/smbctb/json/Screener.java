package yokwe.stock.jp.smbctb.json;

import java.math.BigDecimal;

import yokwe.util.StringUtil;
import yokwe.util.json.JSON.Name;

public final class Screener {
    public static final class Rows {
        public @Name("currencyId")     String currencyId;     // STRING STRING
        public @Name("customFundName") String customFundName; // STRING STRING
        public @Name("isin")           String isin;           // STRING STRING
        public @Name("secId")          String secId;          // STRING STRING

        @Override
        public String toString() {
            return StringUtil.toString(this);
        }
    }

    public @Name("page")     BigDecimal page;     // NUMBER INT
    public @Name("pageSize") BigDecimal pageSize; // NUMBER INT
    public @Name("rows")     Rows[]     rows;     // ARRAY 168
    public @Name("total")    BigDecimal total;    // NUMBER INT

    @Override
    public String toString() {
        return StringUtil.toString(this);
    }
}
