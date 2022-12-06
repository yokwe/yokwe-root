package yokwe.stock.jp.xbrl.tdnet.label;

import java.util.Map;
import java.util.TreeMap;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import yokwe.util.UnexpectedException;

@XmlRootElement(name = "linkbase")
public class Linkbase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	@XmlElement(name = "labelLink")
	public LabelLink labelLink;
	
	@Override
	public String toString() {
		return String.format("%s", labelLink);
	}
	
	public void stats() {
		logger.info("==== stats");
		for(Label.Lang lang: langRoleHrefValueMap.keySet()) {
			Map<Label.Role, Map<String, String>> roleHrefMap = langRoleHrefValueMap.get(lang);
			for(Label.Role role: roleHrefMap.keySet()) {
				int size = roleHrefMap.get(role).size();
				logger.info("stats {}", String.format("%-2s  %-30s %5d", lang, role, size));
			}
		}
	}
	
	private final Map<Label.Lang, Map<Label.Role, Map<String, String>>> langRoleHrefValueMap = new TreeMap<>();
	
	public String getLabel(Label.Lang lang, Label.Role role, String href) {
		if (langRoleHrefValueMap.containsKey(lang)) {
			Map<Label.Role, Map<String, String>> roleHrefMap = langRoleHrefValueMap.get(lang);
			if (roleHrefMap.containsKey(role)) {
				Map<String, String> hrefMap = roleHrefMap.get(role);
				if (hrefMap.containsKey(href)) {
					return hrefMap.get(href);
				} else {
					logger.error("Unknown href {} {} {}", lang, role, href);
					throw new UnexpectedException("Unknown href");
				}
			} else {
				logger.error("Unknown role {} {} {}", lang, role, href);
				throw new UnexpectedException("Unknown role");
			}
		} else {
			logger.error("Unknown lang {} {} {}", lang, role, href);
			throw new UnexpectedException("Unknown lang");
		}
	}
	
	void afterUnmarshal(Unmarshaller u, Object parent) {
		// Sanity check
		if (labelLink == null) {
			logger.error("labelLink is null {}", this);
			throw new UnexpectedException("labelLink is null");
		}
		
		// Build locMap
		Map<String, String> locMap = new TreeMap<>(); // label => href
		{
			for(Loc e: labelLink.locList) {
				if (locMap.containsKey(e.label)) {
					logger.error("Duplicate label {}", e);
					throw new UnexpectedException("Duplicate label");
				} else {
					locMap.put(e.label, e.href);
				}
			}
		}
		
		// Build labelArcMap
		Map<String, String> labelArcMap = new TreeMap<>(); // to (label.label) => from (loc.label)
		{
			for(LabelArc e: labelLink.labelArcList) {
				if (labelArcMap.containsKey(e.to)) {
					logger.error("Duplicate labelArc.to {}", e);
					throw new UnexpectedException("Duplicate labelArc.to");
				} else {
					labelArcMap.put(e.to, e.from);
				}
			}
		}

		// Build langRoleHrefValueMap
		{
			for(Label e: labelLink.labelList) {
				Label.Lang lang  = e.lang;
				Label.Role role  = e.role;
				String     value = e.value;
				String     href;
				
				{
					String labelLabel = e.label;
					String locLabel = labelArcMap.get(labelLabel);
					if (locLabel == null) {
						logger.error("No entry labelArcMap {}", labelLabel);
						throw new UnexpectedException("No entry labelArcMap");
					}
					href = locMap.get(locLabel);
					if (href == null) {
						logger.error("No entry locMap {}", locLabel);
						throw new UnexpectedException("No entry locMap");
					}
				}

				// 	private final Map<Label.Lang, Map<Label.Role, Map<String, String>>> langRoleHrefValueMap = new TreeMap<>();
				if (!langRoleHrefValueMap.containsKey(lang)) {
					langRoleHrefValueMap.put(lang, new TreeMap<>());
				}
				Map<Label.Role, Map<String, String>> roleHrefValueMap = langRoleHrefValueMap.get(lang);
				if (!roleHrefValueMap.containsKey(role)) {
					roleHrefValueMap.put(role, new TreeMap<>());
				}
				Map<String, String> hrefValueMap = roleHrefValueMap.get(role);
				
				if (hrefValueMap.containsKey(href)) {
					logger.error("Duplicate key hrefValueMap {}", href);
					throw new UnexpectedException("Duplicate key hrefValueMap");
				} else {
					hrefValueMap.put(href, value);
				}
			}
		}
	}
}

//<linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrli="http://www.xbrl.org/2003/instance">
//  <labelLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
//    <loc xlink:type="locator" xlink:href="tse-ed-t-2014-01-12.xsd#tse-ed-t_AmountChangeGrossOperatingRevenues" xlink:label="AmountChangeGrossOperatingRevenues"/>
//    <label xlink:type="resource" xlink:label="label_AmountChangeGrossOperatingRevenues" xlink:role="http://www.xbrl.org/2003/role/label" xml:lang="ja" id="label_AmountChangeGrossOperatingRevenues">増減額</label>
//    <labelArc xlink:type="arc" xlink:arcrole="http://www.xbrl.org/2003/arcrole/concept-label" xlink:from="AmountChangeGrossOperatingRevenues" xlink:to="label_AmountChangeGrossOperatingRevenues"/>
