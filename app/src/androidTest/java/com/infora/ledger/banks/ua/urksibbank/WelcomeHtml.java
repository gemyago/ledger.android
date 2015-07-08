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

    public static String contentsWithErrorMessage() {
        return "<div class=\"entrance-content\" id=\"autorization\">\n" +
                "\t<div class=\"login-wrapper\">\n" +
                "\t\t<div class=\"error-login\">\n" +
                "\t\t\t<h2 class=\"message\" id=\"endSession\" style=\"display: none;\">session error message.</h2>\n" +
                "\t\t\t<h2 class=\"message\">other authentication failure message.</h2><span id=\"showEndSessionMessaage\">false</span>\n" +
                "\t\t</div>\n" +
                "\t</div>\n" +
                "</div>";
    }

    public static String contentsWithHiddenEndSessionErrorMessage() {
        return "<div class=\"entrance-content\" id=\"autorization\">\n" +
                "\t<div class=\"login-wrapper\">\n" +
                "\t\t<div class=\"error-login\">\n" +
                "\t\t\t<h2 class=\"message\" id=\"endSession\" style=\"display: none;\">session error message.</h2>\n" +
                "\t\t</div>\n" +
                "\t</div>\n" +
                "</div>";
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

    public static String contentsWithTransactions() {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1251\">\n" +
                "    <title>Star24</title>\n" +
                "</head>\n" +
                "<body id=\"uk\">\n" +
                "<div id=\"wrapper\">\n" +
                "    <table class=\"externalTable\">\n" +
                "        <tbody id=\"cardAccountInfoForm:j_id_jsp_1610737686_136:tbody_element\">\n" +
                "        <tr>\n" +
                "            <td>\n" +
                "                <table class=\"opersTable\">\n" +
                "                    <caption>?????? MasterCard Debit 111111****2222 SURNAME NAME</caption>\n" +
                "                    <thead>\n" +
                "                    <tr>\n" +
                "                        <th>???? ????????</th>\n" +
                "                        <th>???? ??????????</th>\n" +
                "                        <th>??? ???????????</th>\n" +
                "                        <th>???? ????????</th>\n" +
                "                        <th>??????<br>????????</th>\n" +
                "                        <th>???? ?<br>?????? ????????</th>\n" +
                "                        <th>???? ?<br>?????? ???????</th>\n" +
                "                    </tr>\n" +
                "                    </thead>\n" +
                "                    <tbody id=\"cardAccountInfoForm:j_id_jsp_1610737686_136:0:j_id_jsp_1610737686_139:tbody_element\">\n" +
                "                    <tr class=\"darkRow\">\n" +
                "                        <td class=\"dateColumn\">12.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">16.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">605357</td>\n" +
                "                        <td>????????? ??????? ? ????????? ?????-????????\\ATM80524\\UA\\KHARKIV\\GEROI\\GEROIV TRUDA A\n" +
                "                        </td>\n" +
                "                        <td class=\"currencyColumn\">UAH</td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-500.00</nobr>\n" +
                "                        </td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-500.00</nobr>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr class=\"brightRow\">\n" +
                "                        <td class=\"dateColumn\">17.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">18.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">154670</td>\n" +
                "                        <td>????????? ??????? ? ????????? ?????\\A0308854\\UA\\KHARKIV\\UKRSIBBANK</td>\n" +
                "                        <td class=\"currencyColumn\">UAH</td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-4 000.00</nobr>\n" +
                "                        </td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-4 000.00</nobr>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table class=\"outcomeTable\">\n" +
                "                    <tbody>\n" +
                "                    <tr class=\"darkRow \">\n" +
                "                        <th>????? ???????? ??????</th>\n" +
                "                        <td class=\"amountColumn\">-4 500.00</td>\n" +
                "                    </tr>\n" +
                "                    <tr class=\"brightRow \">\n" +
                "                        <th>????? ?????????</th>\n" +
                "                        <td class=\"amountColumn\">0.00</td>\n" +
                "                    </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>\n" +
                "                <table class=\"opersTable\">\n" +
                "                    <caption>?????? MasterCard Debit 333333****4444 SURNAME NAME</caption>\n" +
                "                    <thead>\n" +
                "                    <tr>\n" +
                "                        <th>???? ????????</th>\n" +
                "                        <th>???? ??????????</th>\n" +
                "                        <th>??? ???????????</th>\n" +
                "                        <th>???? ????????</th>\n" +
                "                        <th>??????<br>????????</th>\n" +
                "                        <th>???? ?<br>?????? ????????</th>\n" +
                "                        <th>???? ?<br>?????? ???????</th>\n" +
                "                    </tr>\n" +
                "                    </thead>\n" +
                "                    <tbody id=\"cardAccountInfoForm:j_id_jsp_1610737686_136:1:j_id_jsp_1610737686_139:tbody_element\">\n" +
                "                    <tr class=\"darkRow\">\n" +
                "                        <td class=\"dateColumn\">04.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">08.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">92963Z</td>\n" +
                "                        <td>?????? ???????\\??????\\S1HA0HFD\\UA\\DERGACHI\\KHAR\\7YABOYKO</td>\n" +
                "                        <td class=\"currencyColumn\">UAH</td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-200.00</nobr>\n" +
                "                        </td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-200.00</nobr>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr class=\"brightRow\">\n" +
                "                        <td class=\"dateColumn\">04.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">08.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">97033Z</td>\n" +
                "                        <td>?????? ???????\\??????\\S1HA0MIV\\UA\\KHARKOV\\KHARK\\KLASSKORKA</td>\n" +
                "                        <td class=\"currencyColumn\">UAH</td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-90.04</nobr>\n" +
                "                        </td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-90.04</nobr>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr class=\"darkRow\">\n" +
                "                        <td class=\"dateColumn\">05.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">08.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">669474</td>\n" +
                "                        <td>????????? ??????? ? ????????? ?????\\A0308854\\UA\\KHARKIV\\UKRSIBBANK</td>\n" +
                "                        <td class=\"currencyColumn\">UAH</td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-5 000.00</nobr>\n" +
                "                        </td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-5 000.00</nobr>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr class=\"brightRow\">\n" +
                "                        <td class=\"dateColumn\">06.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">09.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">01310Z</td>\n" +
                "                        <td>?????? ???????\\??????\\S1HA0HFD\\UA\\DERGACHI\\KHAR\\7YABOYKO</td>\n" +
                "                        <td class=\"currencyColumn\">UAH</td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-4 288.00</nobr>\n" +
                "                        </td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-4 288.00</nobr>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr class=\"darkRow\">\n" +
                "                        <td class=\"dateColumn\">06.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">09.06.2015</td>\n" +
                "                        <td class=\"dateColumn\">02671Z</td>\n" +
                "                        <td>?????? ???????\\??????\\S1HA0HFD\\UA\\DERGACHI\\KHAR\\7YABOYKO</td>\n" +
                "                        <td class=\"currencyColumn\">USD</td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-100.00</nobr>\n" +
                "                        </td>\n" +
                "                        <td class=\"amountColumn\">\n" +
                "                            <nobr>-815.23</nobr>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "        </tbody>\n" +
                "    </table>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }
}
