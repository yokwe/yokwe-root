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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.util.CSVUtil;

public final class Fund implements Comparable<Fund> {
	public static final String FILE_PATH = "tmp/data/smbc/fund.csv";
	
	public static List<Fund> getList() {
		List<Fund> list = CSVUtil.read(Fund.class).file(FILE_PATH);
		return (list == null) ? new ArrayList<>() : list;
	}
	public static Map<String, Fund> getMap() {
		return getList().stream().collect(Collectors.toMap(Fund::getSecId, Function.identity()));
	}
	
	public static void save(List<Fund> list) {
		if (list.isEmpty()) return;
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Fund.class).file(FILE_PATH, list);
	}

	public enum Currency {
		AUD, EUR, JPY, USD
	}
	
    public String   secId;
    public String   isin;
    public Currency currency;
    public String   fundName;
    
    public Fund() {
    	this(null, null, null, null);
    }
    public Fund(String secId, String isin, Currency currency, String fundName) {
    	this.secId    = secId;
    	this.isin     = isin;
    	this.currency = currency;
    	this.fundName = fundName;
    }
    
    public String getSecId() {
    	return secId;
    }
    
    @Override
    public String toString() {
    	return String.format("{%s %s %s %s}", secId, isin.isEmpty() ? "-" : isin, currency, fundName);
    }
    
	@Override
	public int compareTo(Fund that) {
		return this.secId.compareTo(that.secId);
	}
}
