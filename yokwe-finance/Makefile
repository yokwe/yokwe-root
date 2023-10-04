#
#
#


DATA_PATH_FILE := data/DataPathLocation
DATA_PATH := $(shell cat $(DATA_PATH_FILE))

.PHONY: all build check_data_path


all: check_data_path
	@echo "all"


update-crontab:
	crontab data/crontab


build:
	( cd ..; mvn ant:ant install )

full-build:
	( cd ..; mvn clean ant:ant install )


check_data_path:
	@echo "DATA_PATH_FILE  !$(DATA_PATH_FILE)!"
	@echo "DATA_PATH       !$(DATA_PATH)!"
	@if [ ! -d $(DATA_PATH) ]; then \
	  	  echo "no directory  !${DATA_PATH}!" ; \
	  exit 1 ; \
	fi

rync-to-sandisk2t:
	rsync -a -P /Volumes/finance/ /Volumes/SanDisk2T/finance


# maintenance of save and log
clear-save-file: check_data_path
	find ${DATA_PATH}/save     -mtime +30 -print -delete

clear-log-file: check_data_path
	tar cfz $(DATA_PATH)/save/log_$$(date +%Y%m%d).taz tmp/*.log
	echo -n >tmp/yokwe-finance.log
	echo -n >tmp/cron.log

save-all: check_data_path save-stock save-provider

save-stock: check_data_path
	cd $(DATA_PATH); tar cfz save/stock_$$(date +%Y%m%d).taz    stock

save-provider: check_data_path
	cd $(DATA_PATH); tar cfz save/provider_$$(date +%Y%m%d).taz provider


#
# REIT JP
#
update-reit-div-jp:
	ant provider-jreit-update-reit-info
	ant provider-jreit-update-reit-div
	

#
# ETF JP
#
update-etf-div-jp:
	ant provider-manebu-update-etf-div-info
	

#
# FUND JP
#
update-fund-jp:
	ant provider-jita-update-fund-info
	ant provider-jita-update-fund-div-price
	

#
# STOCK JP
#
update-jpx-listing:
	ant provider-jpx-update-listring	

update-stock-jp-info:
	ant provider-jpx-update-etf
	ant provider-jpx-update-etn
	ant provider-jpx-update-foreign-stock
	ant provider-jpx-update-infra-fund
	ant provider-jpx-update-reit
	ant provider-jpx-update-stock-info

update-stock-jp-price:
	ant provider-jpx-update-stock-price
	ant stock-jp-update-stock-price


#
# STOCK US
#
update-stock-us-info: update-trading-sock
	ant provider-bats-update-stock-info
	ant provider-nasdaq-update-stock-info
	ant provider-nyse-update-stock-info
	ant stock-us-update-stock-info

update-trading-sock:
	ant provider-monex-update-trading-stock
	ant provider-moomoo-update-trading-stock
	ant provider-rakuten-update-trading-stock
	ant provider-sbi-update-trading-stock

update-stock-us-price:
	ant stock-us-update-stock-price

update-stock-us-div:
	ant stock-us-update-stock-div
