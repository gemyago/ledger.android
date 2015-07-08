package com.infora.ledger.banks.ua.urksibbank;

/**
 * Created by mye on 7/8/2015.
 */
public class WelcomeHtml {
    public static String contentsWithViewState() {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1251\">\n" +
                "    <title>Star24</title>\n" +
                "</head>\n" +
                "<body id=\"uk\">\n" +
                "<div id=\"wrapper\">\n" +
                "    <div id=\"branding\">\n" +
                "        <div id=\"new_top\">\n" +
                "            <a id=\"logo\" href=\"/web_banking/\">\n" +
                "                <img src=\"/web_banking/img/logo_ua.png\"/>\n" +
                "            </a>\n" +
                "\n" +
                "            <div class=\"head-main-url\">\n" +
                "                <form id=\"menu:localeForm\" name=\"menu:localeForm\" method=\"post\"\n" +
                "                      action=\"/web_banking/protected/reports/sap_card_account_info.jsf\"\n" +
                "                      enctype=\"application/x-www-form-urlencoded\" class=\"localeFormFloat clear-fix\">\n" +
                "                    <input type=\"hidden\" name=\"menu:localeForm_SUBMIT\" value=\"1\"/>\n" +
                "                    <input type=\"hidden\" name=\"javax.faces.ViewState\" id=\"javax.faces.ViewState\"\n" +
                "                           value=\"the-view-state-value\"/>\n" +
                "                </form>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }
}
