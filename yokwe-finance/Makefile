#
#
#

DATA_PATH_FILE := data/DataPathLocation
DATA_PATH := $(shell cat $(DATA_PATH_FILE))

SELENIUM_DATA_PATH_FILE := data/SeleniumDataPathLocation
SELENIUM_DATA_PATH      := $(shell cat $(SELENIUM_DATA_PATH_FILE))
CHROME_FOR_TESTING_DIR  := $(SELENIUM_DATA_PATH)/chrome-for-testing
CHROME_FOR_TESTING_VERSION := $(shell cat $(CHROME_FOR_TESTING_DIR)/chrome-version)

.PHONY: all build check-data-path check-selenium-data-path


all: check-data-path check-selenium-data-path check-chrome-for-testing
	@echo "DATA_PATH                   $(DATA_PATH)"
	@echo "SELENIUM_DATA_PATH          $(SELENIUM_DATA_PATH)"
	@echo "CHROME_FOR_TESTING_DIR      $(CHROME_FOR_TESTING_DIR)"
	@echo "CHROME_FOR_TESTING_VERSION  $(CHROME_FOR_TESTING_VERSION)"


update-crontab:
	crontab data/crontab

open-chrome-for-testing: check-chrome-for-testing
	open -n '$(CHROME_FOR_TESTING_DIR)/chrome-mac-arm64/Google Chrome for Testing.app' --args --user-data-dir=$(CHROME_FOR_TESTING_DIR)/user-data-dir


build:
	( cd ..; mvn ant:ant install )

full-build:
	( cd ..; mvn clean ant:ant install )


check-data-path:
#	@echo "DATA_PATH_FILE  !$(DATA_PATH_FILE)!"
#	@echo "DATA_PATH       !$(DATA_PATH)!"
	@if [ ! -d $(DATA_PATH) ]; then \
		echo "DATA_PATH  no directory  !${DATA_PATH}!" ; \
		exit 1 ; \
	fi


check-selenium-data-path:
#	@echo "SELENIUM_DATA_PATH_FILE  !$(SELENIUM_DATA_PATH_FILE)!"
#	@echo "SELENIUM_DATA_PATH       !$(SELENIUM_DATA_PATH)!"
	@if [ ! -d $(SELENIUM_DATA_PATH) ]; then \
		echo "SELENIUM_DATA_PATH  no directory  !${SELENIUM_DATA_PATH}!" ; \
		exit 1 ; \
	fi

check-chrome-for-testing:
#	@echo "CHROME_FOR_TESTING_VERSION  $(CHROME_FOR_TESTING_VERSION)"
#	@echo "CHROME_FOR_TESTING_DIR      $(CHROME_FOR_TESTING_DIR)"
	@if [ ! -d $(CHROME_FOR_TESTING_DIR) ]; then \
		echo "CHROME_FOR_TESTING_DIR  no directory  !${CHROME_FOR_TESTING_DIR}!" ; \
		exit 1 ; \
	fi


log-start:
	@date +'%F %T LOG START $(LOG_TITLE)'

log-stop:
	@date +'%F %T LOG STOP  $(LOG_TITLE)'

log-time:
	@date +'%F %T LOG TIME'

check-temp-file: check-data-path
	find . $(DATA_PATH) -regex '.*/\.DS.*'           -print
	find . $(DATA_PATH) -regex '.*/\.[^a-zA-Z0-9].*' -print

rsync-to-Backup2T: check-data-path
	@date +'%F %T RSYNC START'
	rsync -ah --delete /Volumes/SanDisk2T/* /Volumes/Backup2T/
	@date +'%F %T RSYNC STOP'

kill-soffice:
	@ps xg > tmp/kill_soffice
	@awk '/LibreOffice/ && /soffice/ {print; system("killall -v soffice"); exit;}' tmp/kill_soffice

kill-selenium-driver:
	@ps xg > tmp/kill_selenium_driver
	@awk '/safaridriver/ {print; system("killall -v safaridriver"); exit;}' tmp/kill_selenium_driver
	@awk '/chromedriver/ {print; system("killall -v chromedriver"); exit;}' tmp/kill_selenium_driver
	
# maintenance of save and log
clear-save-file: check-data-path
	find ${DATA_PATH}/save -mtime +7d -print -delete

clear-report-file: check-data-path
	find ${DATA_PATH}/report -name '*ods' -and -mtime +7d -print -delete

clear-log-file: check-data-path
	@date +'%F %T TAR START'
	tar cfz $(DATA_PATH)/save/log_$$(date +%Y%m%d).taz tmp/*.log
	@date +'%F %T TAR STOP'
	echo -n >tmp/yokwe-finance.log
	echo -n >tmp/cron.log

save-all: check-data-path save-stock save-provider save-report rsync-to-Backup2T

save-stock: check-data-path
	@date +'%F %T TAR START'
	cd $(DATA_PATH); tar cfz save/stock_$$(date +%Y%m%d).taz    stock
	@date +'%F %T TAR STOP'

save-provider: check-data-path
	@date +'%F %T TAR START'
	cd $(DATA_PATH); tar cfz save/provider_$$(date +%Y%m%d).taz provider
	@date +'%F %T TAR STOP'

save-report: check-data-path
	@date +'%F %T TAR START'
	cd $(DATA_PATH); tar cfz save/report_$$(date +%Y%m%d).taz report
	@date +'%F %T TAR STOP'


#
# FX-RATE
#
update-fx-rate:
	ant provider-mizuho-update-fx-rate


#
# UPDATE ASSET
#
update-asset-all:
	ant account-update-asset-all


#
# JREIT
#
update-jreit:
	ant provider-jreit-update-jreit-info
	ant provider-jreit-update-jreit-div


#
# MANEBU
#
update-manebu:
	ant provider-manebu-update-etf-div-info
	

#
# FUND
#
update-trading-fund:
	ant provider-nikko-update-fund-info-nikko
	ant provider-nikko-update-trading-fund-nikko
	ant provider-prestia-update-trading-fund-prestia
	ant provider-rakuten-update-trading-fund-rakuten
	ant provider-smtb-update-trading-fund-smtb
	ant provider-sony-update-trading-fund-sony

update-fund: check-data-path
	ant provider-jita-update-fund-info-jita
	ant provider-jita-update-fund-div-price-jita
	cp -p $(DATA_PATH)/provider/jita/fund-info-jita.csv $(DATA_PATH)/fund/fund-info.csv
	@date +'%F %T RSYNC START'
	rsync -ah --delete $(DATA_PATH)/provider/jita/fund-price-jita/ $(DATA_PATH)/fund/fund-price
	@date +'%F %T RSYNC START'
	rsync -ah --delete $(DATA_PATH)/provider/jita/fund-div-jita/   $(DATA_PATH)/fund/fund-div
	@date +'%F %T RSYNC STOP'

# after update fund-info and stock-info
update-nisa: check-data-path
	ant provider-jita-update-nisa-info-jita
	cp -p $(DATA_PATH)/provider/jita/nisa-info-jita.csv $(DATA_PATH)/fund/nisa-info.csv

#
# STOCK JP
#
update-stock-info-jp: check-data-path
	ant provider-jpx-update-etf
	ant provider-jpx-update-etn
	ant provider-jpx-update-foreign-stock
	ant provider-jpx-update-infra-fund
	ant provider-jpx-update-reit
	ant provider-jpx-update-stock-split
	ant provider-jpx-update-stock-list
	ant provider-jpx-update-stock-detail
	mv $(DATA_PATH)/provider/jpx/stock-info-jpx.csv $(DATA_PATH)/provider/jpx/stock-info-jpx.csv-SAVE
	ant provider-jpx-update-stock-info-jpx
	cp -p $(DATA_PATH)/provider/jpx/stock-info-jpx.csv $(DATA_PATH)/stock/stock-info-jp.csv
	ant provider-yahoo-update-company-info-jp-yahoo
	
update-stock-price-jp: check-data-path
	ant provider-jpx-update-stock-detail
	ant provider-jpx-update-stock-price-jpx
	@date +'%F %T RSYNC START'
	rsync -ah --delete $(DATA_PATH)/provider/jpx/stock-price-jpx/ $(DATA_PATH)/stock/stock-price-jp
	@date +'%F %T RSYNC STOP'

update-stock-div-jp:
	ant provider-jpx-update-stock-div-jpx
	ant stock-update-stock-div-jp
	

#
# STOCK US
#
update-stock-info-us:
	ant provider-bats-update-stock-info-bats
	ant provider-nasdaq-update-stock-info-nasdaq
	ant provider-nyse-update-stock-info-nyse
	ant stock-update-stock-info-us

update-trading-stock:
	ant provider-nikko-update-trading-stock-nikko
	ant provider-rakuten-update-trading-stock-rakuten

update-stock-price-us:
	ant stock-update-stock-price-us

update-stock-div-us:
	ant stock-update-stock-div-us


#
# REPORT
#

report-stock-stats-us: kill-soffice
	ant report-update-stock-stats-us

report-stock-stats-us-monthly: kill-soffice
	ant report-update-stock-stats-us-monthly

report-stock-stats-jp: kill-soffice
	ant report-update-stock-stats-jp

report-fund-stats: kill-soffice
	ant report-update-fund-stats

report-asset-stats: kill-soffice
	ant report-update-asset-stats
	