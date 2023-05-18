package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;


//Morningstar
//https://web.stanford.edu/~wfsharpe/art/stars/stars2.htm

//GIPS
//https://www.pwc.ch/en/publications/2020/PwC-GIPS-2020.pdf
//https://www.gipsstandards.org/wp-content/uploads/2021/03/calculation_methodology_gs_2011.pdf
//https://www.nbim.no/contentassets/98750345a7e641558d820573c52e4a8e/2020-gips-manual-in-english.pdf
//https://www.cfainstitute.org/-/media/documents/code/gips/gips-standards-for-firms-explanation-of-provisions-section-2.ashx

//Modified Dietz method
//https://en.wikipedia.org/wiki/Modified_Dietz_method

public interface Finance {
	public Map<LocalDate, BigDecimal> monthlyRetrun(Map<LocalDate, BigDecimal> priceList, Map<LocalDate, BigDecimal> divList) ;
	
}
