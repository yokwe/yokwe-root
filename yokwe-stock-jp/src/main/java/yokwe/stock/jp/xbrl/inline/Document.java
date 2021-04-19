package yokwe.stock.jp.xbrl.inline;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import yokwe.stock.jp.tdnet.SummaryFilename;
import yokwe.util.xml.QValue;
import yokwe.util.xml.XMLStream;

public class Document {
	private static final List<InlineXBRL> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>());

	public  final SummaryFilename               filename;
	private final List<InlineXBRL>              all;
	private final Map<QValue, List<InlineXBRL>> map;
	
	private Document(File file, List<InlineXBRL> all, Map<QValue, List<InlineXBRL>> map) {
		this.filename = SummaryFilename.getInstance(file.getName());
		this.all      = all;
		this.map      = map;
	}

	private static void buildMap(List<InlineXBRL> all, Map<QValue, List<InlineXBRL>> map, InlineXBRL ix) {
		all.add(ix);
		
		QValue qName = ix.qName;
		
		List<InlineXBRL> list;
		if (map.containsKey(qName)) {
			list = map.get(qName);
		} else {
			list = new ArrayList<>();
			map.put(qName, list);
		}
		list.add(ix);
	}
	
	public static Document getInstance(File file) {
		List<InlineXBRL>              all = new ArrayList<>();
		Map<QValue, List<InlineXBRL>> map = new TreeMap<>();
		
		XMLStream.buildStream(file).filter(InlineXBRL::canGetInstance).forEach(o -> buildMap(all, map, InlineXBRL.getInstance(o)));
		
		return new Document(file, all, map);
	}

	public List<InlineXBRL> getList() {
		return all;
	}
	public Stream<InlineXBRL> getStream() {
		return getList().stream();
	}
	
	public List<InlineXBRL> getList(QValue qName) {
		if (map.containsKey(qName)) {
			return map.get(qName);
		} else {
			return EMPTY_LIST;
		}
	}
	public Stream<InlineXBRL> getStream(QValue qName) {
		List<InlineXBRL> list = getList(qName);
		return list.stream();
	}
	
	private Set<String> contextSet = null;
	public Set<String> getContextSet() {
		if (contextSet == null) {
			Set<String> set = new TreeSet<>();
			getList().stream().forEach(o -> set.addAll(o.contextSet));
			contextSet = Collections.unmodifiableSet(set);
		}
		return contextSet;
	}
}