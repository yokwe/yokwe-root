/*******************************************************************************
 * Copyright (c) 2020, Yasuhiro Hasegawa
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. The name of the author may not be used to endorse or promote products derived
 *      from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *******************************************************************************/
package yokwe.stock.jp.smbctb.json;

import yokwe.util.StringUtil;
import yokwe.util.json.JSON.Name;

public final class Price {
    public static final class TimeSeries {
        public static final class Security {
            public static final class HistoryDetail {
                public @Name("EndDate") String endDate; // STRING DATE
                public @Name("Value")   String value;   // STRING INT

                @Override
                public String toString() {
                    return StringUtil.toString(this);
                }
            }

            public @Name("HistoryDetail") HistoryDetail[] historyDetail; // ARRAY 3612
            public @Name("Id")            String          id;            // STRING STRING

            @Override
            public String toString() {
                return StringUtil.toString(this);
            }
        }

        public @Name("Security") Security[] security; // ARRAY 1

        @Override
        public String toString() {
            return StringUtil.toString(this);
        }
    }

    public @Name("TimeSeries") TimeSeries timeSeries; // OBJECT

    @Override
    public String toString() {
        return StringUtil.toString(this);
    }
}

