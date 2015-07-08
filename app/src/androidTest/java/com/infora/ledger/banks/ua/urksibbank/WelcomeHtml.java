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

    public static String contentsWithAccounts() {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1251\">\n" +
                "    <title>Star24</title>\n" +
                "</head>\n" +
                "<body id=\"uk\">\n" +
                "<div id=\"wrapper\">\n" +
                "    <div id=\"menu_info\">\n" +
                "        <div id=\"info\">\n" +
                "            <div class=\"scrollable\">\n" +
                "                <form enctype=\"application/x-www-form-urlencoded\"\n" +
                "                      action=\"/web_banking/protected/welcome.jsf\" method=\"post\" name=\"welcomeForm\"\n" +
                "                      id=\"welcomeForm\">\n" +
                "                    <div class=\"scrollable\">\n" +
                "                        <table class=\"current-accounts\">\n" +
                "                            <caption><span>??????? ?? ???????? ???????</span></caption>\n" +
                "                            <thead>\n" +
                "                            <tr>\n" +
                "                                <th></th>\n" +
                "                                <th colspan=\"2\">???????i? ???????</th>\n" +
                "                                <th>?????</th>\n" +
                "                                <th>??????</th>\n" +
                "                                <th>???????</th>\n" +
                "                                <th class=\"actionColumn\">???????</th>\n" +
                "                            </tr>\n" +
                "                            </thead>\n" +
                "                            <tbody id=\"welcomeForm:j_id_jsp_692165209_58:tbody_element\">\n" +
                "                            <tr onclick=\"showContextMenu(this)\" class=\"darkRow\">\n" +
                "                                <td class=\"tariffColumn\">\n" +
                "                                    <a title=\"??????i ????i\" target=\"_blank\"\n" +
                "                                       href=\"https://my.ukrsibbank.com/ua/personal/deposits/active_money/\"></a>\n" +
                "                                </td>\n" +
                "                                <td class=\"aliasColumn\">\n" +
                "                                    <a title=\"?????????? ???????\"\n" +
                "                                       onclick=\"return oamSubmitForm('welcomeForm','welcomeForm:j_id_jsp_692165209_58:0:j_id_jsp_692165209_64',null,[['accountId','11112222']]);\"\n" +
                "                                       href=\"#\">?i? ???????? ??????? ?1</a>\n" +
                "                                </td>\n" +
                "                                <td class=\"imageColumn\">\n" +
                "                                    <a title=\"?????????? ???????i?\"\n" +
                "                                       onclick=\"var cf = function(){javascript: window.open('/web_banking/protected/edit_alias.jsf?type=ACCOUNT&amp;id=11112222&amp;amount=28.51', '',\n" +
                "                                                   'width=600, height=56, left=100,top=100, resizable=no'); return false;};var oamSF = function(){return oamSubmitForm('welcomeForm','welcomeForm:j_id_jsp_692165209_58:0:j_id_jsp_692165209_68');};return (cf()==false)? false : oamSF();\"\n" +
                "                                       href=\"#\"></a>\n" +
                "                                </td>\n" +
                "                                <td class=\"accountColumn\">\n" +
                "                                    <a title=\"?????????? ???????\"\n" +
                "                                       onclick=\"return oamSubmitForm('welcomeForm','welcomeForm:j_id_jsp_692165209_58:0:j_id_jsp_692165209_71',null,[['accountId','11112222']]);\"\n" +
                "                                       href=\"#\">33334444555566</a>\n" +
                "                                </td>\n" +
                "                                <td class=\"currencyColumn\">UAH</td>\n" +
                "                                <td class=\"amountColumn\">99.43</td>\n" +
                "                                <td class=\"paymentColumn\">\n" +
                "                                    <a title=\"?????????? ??????\"\n" +
                "                                       onclick=\"return oamSubmitForm('welcomeForm','welcomeForm:j_id_jsp_692165209_58:0:j_id_jsp_692165209_82',null,[['debitAccount','11112222'],['docType','doc/ua_owner_transfer']]);\"\n" +
                "                                       href=\"#\"></a></td>\n" +
                "                            </tr>\n" +
                "                            <tr onclick=\"showContextMenu(this)\" class=\"brightRow\">\n" +
                "                                <td class=\"tariffColumn\">\n" +
                "                                    <a title=\"?? Start\" target=\"_blank\"\n" +
                "                                       href=\"https://my.ukrsibbank.com/ua/personal/cards/salary_cards/\"></a>\n" +
                "                                </td>\n" +
                "                                <td class=\"aliasColumn\">\n" +
                "                                    <a title=\"?????????? ???????\"\n" +
                "                                       onclick=\"return oamSubmitForm('welcomeForm','welcomeForm:j_id_jsp_692165209_58:1:j_id_jsp_692165209_64',null,[['accountId','77778888']]);\"\n" +
                "                                       href=\"#\">?i? ???????? ??????? ?2</a>\n" +
                "                                </td>\n" +
                "                                <td class=\"imageColumn\">\n" +
                "                                    <a title=\"?????????? ???????i?\"\n" +
                "                                       onclick=\"var cf = function(){javascript: window.open('/web_banking/protected/edit_alias.jsf?type=ACCOUNT&amp;id=77778888&amp;amount=23735.21', '',\n" +
                "                                                   'width=600, height=56, left=100,top=100, resizable=no'); return false;};var oamSF = function(){return oamSubmitForm('welcomeForm','welcomeForm:j_id_jsp_692165209_58:1:j_id_jsp_692165209_68');};return (cf()==false)? false : oamSF();\"\n" +
                "                                       href=\"#\"></a>\n" +
                "                                </td>\n" +
                "                                <td class=\"accountColumn\">\n" +
                "                                    <a title=\"?????????? ???????\"\n" +
                "                                       onclick=\"return oamSubmitForm('welcomeForm','welcomeForm:j_id_jsp_692165209_58:1:j_id_jsp_692165209_71',null,[['accountId','77778888']]);\"\n" +
                "                                       href=\"#\">99998888000099</a></td>\n" +
                "                                <td class=\"currencyColumn\">UAH</td>\n" +
                "                                <td class=\"amountColumn\">10 100.43</td>\n" +
                "                                <td class=\"paymentColumn\">\n" +
                "                                    <a title=\"?????????? ??????\"\n" +
                "                                       onclick=\"return oamSubmitForm('welcomeForm','welcomeForm:j_id_jsp_692165209_58:1:j_id_jsp_692165209_82',null,[['debitAccount','77778888'],['docType','doc/ua_owner_transfer']]);\"\n" +
                "                                       href=\"#\"></a>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            </tbody>\n" +
                "                        </table>\n" +
                "\n" +
                "                    </div>\n" +
                "                </form>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }
}
