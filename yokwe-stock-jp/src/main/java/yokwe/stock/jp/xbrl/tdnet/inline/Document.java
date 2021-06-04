package yokwe.stock.jp.xbrl.tdnet.inline;

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
	private static final List<BaseElement> EMPTY_LIST = List.of();

	public  final SummaryFilename                filename;
	private final List<BaseElement>              all;
	private final Map<QValue, List<BaseElement>> map;
	
	private Document(File file, List<BaseElement> all, Map<QValue, List<BaseElement>> map) {
		this.filename = SummaryFilename.getInstance(file.getName());
		this.all      = all;
		this.map      = map;
	}

	private static void buildMap(List<BaseElement> all, Map<QValue, List<BaseElement>> map, BaseElement ix) {
		all.add(ix);
		
		QValue qName = ix.qName;
		
		List<BaseElement> list;
		if (map.containsKey(qName)) {
			list = map.get(qName);
		} else {
			list = new ArrayList<>();
			map.put(qName, list);
		}
		list.add(ix);
	}
	
	public static Document getInstance(File file) {
		List<BaseElement>              all = new ArrayList<>();
		Map<QValue, List<BaseElement>> map = new TreeMap<>();
		
		XMLStream.buildStream(file).filter(BaseElement::canGetInstance).forEach(o -> buildMap(all, map, BaseElement.getInstance(o)));
		
		return new Document(file, all, map);
	}

	public List<BaseElement> getList() {
		return all;
	}
	public Stream<BaseElement> getStream() {
		return getList().stream();
	}
	
	public List<BaseElement> getList(QValue qName) {
		if (map.containsKey(qName)) {
			return map.get(qName);
		} else {
			return EMPTY_LIST;
		}
	}
	public Stream<BaseElement> getStream(QValue qName) {
		List<BaseElement> list = getList(qName);
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