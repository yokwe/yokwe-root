package yokwe.finance.account;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;

public interface UpdateAsset {
	static final String FILE_NAME = "asset.csv";
	
	Storage getStorage();
	void download();
	void update();
	
	
	default File getFile() {
		return getStorage().getFile(FILE_NAME);
	}
	
	default List<Asset> getList() {
		return ListUtil.getList(Asset.class, getFile());
	}
	
	default void save(List<Asset> list) {
		ListUtil.save(Asset.class, getFile(), list);
	}
	default void save(Collection<Asset> collection) {
		var list = collection.stream().collect(Collectors.toList());
		Collections.sort(list);
		save(list);
	}
}
