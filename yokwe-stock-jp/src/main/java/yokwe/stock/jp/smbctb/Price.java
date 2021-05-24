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
package yokwe.stock.jp.smbctb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.stock.jp.smbctb.Fund.Currency;
import yokwe.util.CSVUtil;

public final class Price implements Comparable<Price> {
	public static final String DIR_PATH = "tmp/data/smbc/price"; // FIXME

	public static String getPath(String secId) {
		return String.format("%s/%s.csv", DIR_PATH, secId);
	}
	public static List<Price> load(String secId) {
		return CSVUtil.read(Price.class).file(getPath(secId));
	}
	public static void save(List<Price> list) {
		if (list.isEmpty()) return;
		
		// Sort before save
		Collections.sort(list);
		Price price = list.get(0);
		CSVUtil.write(Price.class).file(getPath(price.secId), list);
	}
	public static List<Price> getList(String secId) {
		List<Price> list = load(getPath(secId));
		return (list == null) ? new ArrayList<>() : list;
	}
	public static Map<String, Price> getMap(String secId) {
		return getList(secId).stream().collect(Collectors.toMap(Price::getDate, Function.identity()));
	}
	private static Map<String, Map<String, Price>> map = new TreeMap<>();
	//                 secId       date
	public static Price getPrice(String secId, String date) {
		Map<String, Price> priceMap;
		if (map.containsKey(secId)) {
			priceMap = map.get(secId);
		} else {
			priceMap = getMap(secId);
			map.put(secId, priceMap);
		}
		if (priceMap.containsKey(date)) {
			return priceMap.get(date);
		} else {
			return null;
		}
	}

	public String     date;  // YYYY-MM-DD
    public String     secId;
    public Currency   currency;
    public BigDecimal value;
    
    public Price(String date, String secId, Currency currency, BigDecimal price) {
    	this.date     = date;
    	this.secId    = secId;
    	this.currency = currency;
    	this.value    = price;
    }
    
    public String getDate() {
    	return date;
    }
    
    @Override
    public String toString() {
    	return String.format("{%s %s %s %s}", date, secId, currency, value.toPlainString());
    }
	@Override
	public int compareTo(Price that) {
		int ret = this.secId.compareTo(that.secId);
		if (ret == 0) ret = this.date.compareTo(that.date);
		return ret;
	}
}
