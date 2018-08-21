package com.athena.services.ina;
/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//import org.json.JSONException;
//import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
//import org.apache.log4j.Logger;

/**
 * Security-related methods. For a secure implementation, all of this code should be implemented on a server that
 * communicates with the application on the device. For the sake of simplicity and clarity of this example, this code is
 * included here and is executed on the device. If you must verify the purchases on the phone, you should obfuscate this
 * code to make it harder for an attacker to replace the code with stubs that treat all purchases as verified.
 */
public class Security {
	public static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl8B7TJO/DiZbZpzbSV9kaHqu9hvpQJrAMfhiCh1vlBSleD7YQ1rzaHHNHkOQTxu/M+j0tDEkNM25I0r0+vJlewVJiIDPauSEi0yzpK55e9W8ISEpKGUsz9YGzbwDDkw4RerM9pUzGACHIi/UlgR3aGNqLL4Jr6jf7N5KDg/c0mtAh2tq31PlDIaeYSi00J9/YLEWrLhGSFw9Z+4XhbMn8mt90qe8WUEvKfpGKxmND4iSLTjVIo5flhrEwaAWAv7ZSuI9zQbALbvFJAe7ywi6/msDKbwD5xny8i5pWg/oe0r2OMAc3PCP/TAuoj9+bbBFxo9plK/Kq3Y+t2NmjIF/ewIDAQAB";
	public static final String base64EncodedPublicKey2 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsdy4T9WftkaOK1wtIJD2fGO/IfEUc4az/hicici8gtZGfNurA4+wtVAgF8drBoK+EoxGGytDn+onmoINFM5Ep86ypf0F9Hz9FYGBpcV+HQcLl8czh+wlXQh/fUjToSXclT46J1CIQhGYoiptTTW8mxpTy80OkInxOMKXUS7DI9UuEYaRLynt3RW2MG46l7kY7V5l1JPEAHYrgSF9X4jYx5bdQ0l/teX1RpSjGN4p3NfUwzLHhrLA6sgWnXckZzWXB+hqGHzMPGp+nA1crmLGyVe8CVMWfe6R/2APrTqtju12aD7MIUOuU2bKBhh5Qhrl3rDokT+JEOYY0X9QoUTRGwIDAQAB";
	public static final String base64EncodedPublicKey3 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAizIyDqyRvOJ4gzoAbjjLo5EtDiRtDMy7/lCIcPKEpDTKdWmF9T86ZO4/ZJtXm+NQ6kfI/8LyWsDw8OWxySK9gZVVTXtdiZAx92qhEMLbJly/rupYCX2MCKwrGBfrhtTeYANuDB8pWYSlFQqPDqe6XfzPxvpbLquulO2LdpGz5E7eECKlrUqm2t/lnUiFHGo1eyIQ+qXirF3N/Oe3tN61j+BNZ+oBqzsNjmnoO3l7gg4o8Mcsn0F5dHPpCgyZv5WuQ5gnsJtzQBfCIYB5NU+6I92YTy7hdBGP7U7lwN6uPendSJU/tkVHuX2/8Z2rqHsaBa2WLpl5OqfHZiXqVXrwlwIDAQAB";
	public static final String base64EncodedPublicKey4 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjI0C+hAvQRwBibUo01qDGQ23HWJqHJhYYA3WiWuXtRM9DAfCMwDmlZ60lVrm+n0kEkrP7vMGhh3TYl5TI40CgdWjOENzSGG04B76RTpvtlbRBN24qexeSnvTwj4UJ6qNB9KyMV+7yXck1m2ufUZQNlSDQ+KcGIFOKzl2zT5l3Tv/jaRQnux8zdbTm8lvXS2lS2uLCoDHcdua53CjKkmarmzPqEYEGBD14MrwSuqPHnXWMcIrlmhVvp84WVeKUH0fExvt56wtmZvx/Cks1Pi3YRkdGpF8WtEdPRFcaPQi6vL0PORjDZVWhh94P2XAozPymJVnjD2hdEyF+Aj4IGoUwwIDAQAB";
	public static final String base64EncodedPublicKey5 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsAee6kRz9hDA9D1hUEaf/liNYdv4p0/wmboKMKJP8DUftucm4R428kiA29P2XJbg3SCsBIubsGELsDnusu4dXwQVH6I8oXUX4piZKwCcigSAJgWTdzsVbFNtZ/dmFNVrogfLPM0sKFl8DQeLWMZDtFNCHIXEbYPpJKjrO7moG9ooTNFm7s+LiLfE9I/uJBwqAF+jyl2fcpNOubd4orHPN2dpqBU8fqkhCUUvd6YqVzvVeHPKsdWwKgOuA76zu/k6WF1IUQP9eQ667KsL5HNZRM6f5Jwhjil3NQM6avDfK/IrduUycXs7cMAJ8m6SMON1thiLfdX5cSpIAaYRhCoUlwIDAQAB";
	public static final String base64EncodedPublicKey1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1g+faanzWe6TmCxOQtZyCxY8oiNcME55j53oHACVfNURmz3rv5Zb5N0uLTXXUph6Fm7VcS4cWxWbOHGNMWdtovNkeN3q6n+JlEekfZgTXKuUrsRH154zOdasUKc7d9sIXqo3CIuIEBOWFcJJF0siakE6ChzyTedt24QLaQqpUygVvHSUAB6B2nnEG1CQL0s57P0pCsSWMpKefxFJsO7Qv2xFwYmcN5P8+zwe+qqo1Y1fud/4v5uNPOh6nk+R3oHBRMg2n9bunTWSfSJXN45+3M/63iQfOj8OCJZ7mTzBFDfCVwN15osGuXNAhRx5NluKu1q5InFtcPXgMwp/cXQyxwIDAQAB";
	public static final String base64EncodedPublicKey6 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsbUYj8L+9R2/xN/6nHWTw9VjvKM13db9RyG8yYDZzY23znw61AEJAoUE+nAF9zsmueEXu8Q295X3N92qhgIL54pwAlENrEVzxMnIcp3n4km3wVYpVF5vODXL74VmmNmh4WcnVayb2ECr0xzegWxynyeoaJW2RYnKe3++nTxkQKltQrXFcIrdLU7WFWMKR1rcPHUfP9JzvtIRmPiuM+K3jr+sDgC8L+m032NDpRN0SNQbodAIhCRionK5KDWXkz/+pKrepJ3FFxUpH2ys6uRuiaev8WPjUdC0s72AYUYi8vKQuBJXQ1hxN4WuFi1L0V+ErHRDTwMjKlw5F27Z5AWPLwIDAQAB";
	public static final String base64EncodedPublicKey7 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyLebygMgMfxx7m3Jq0+pTTltgCCugnzXAf5OJ9vd1R56Z7a7NQKltwfuaMCYV2QVxMVMTjSiQTqJcSkG4b1Bk29ZNEn4cjYXDqE49nS04zTvQQ/qe7EpmRKy8kG5tyja56L5YpYgF6r2dj8FiiGSG9GL43CFgNmVWfMSWYQz1kpBk5M1TGQEXM7U9c2o2clqlVqJ1g0m+rAkya39JTabFfAuDNh6YrmwHOUr7smMYczmIXs7RB2rMwBKoR3k7CIHg/y4HQfDiADA+NsYc4+Ud4w4l3TA2yoNIFXKiQ4VRB3wjyvRXWUl6sgwX5DcEhaPl61d4jft0csjNhfHlbnafQIDAQAB";
	public static final String base64EncodedPublicKey8 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2O9/FTv+Aoe6OnfVogGMXmtDcRNWwFKkw/n0T5aYPOD6nZVDBgQCeIRPViQqSlVhLUGJjwywoKyABIHOdln0TwLjziTzz2ZdBbbVsvwJkQzsHFE4uhGSKcG1O9LwwgIijRv9v3EembyfuMOhNO2d45+yS1DQnOAT85eczO8lrq9vLuyq2HjA5d1Lg1/sx8zHr15e8jao11f/8MBXpEuxKzHHgpwlNp3MLUXO2N8TM1ZSk4GSSn5IzEZEMARjqW1mWvmsVxyy316aUID381jHc26bQBUjiEYoY6ez4mekMJNoghjq9pOmZgEupBDI1pnI9iMMzpE7FieDuCwROeocSwIDAQAB";
	public static final String base64EncodedPublicKey9 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArd4GRZUgsoAiQ5L4GtYX5UA3OXG61PCYCgcFEo6Z53Fe+iJXRmyTREknyn5q2a+ehmtCQBs0owc38WMmyoSd3+eDDX2KlQaqPvUJfkclf/MZYJIn/NDZ8+ZVKlzqBRGl4JdOPWRDeYLla5QCeMdFRZ1oPLv0fvGY4xK6p/zvVyMkwmSWJWu+fUDM6clCarAbCak1Sr9YB18KZIZvQmIw5t+H7kCJ85lnowPqlDW6yYLFGzZpfVgwYbRvnkQeW7ugF/aP4Pt9J7MRO8nnO62JEFPhzqEQpRlUpg9LXqa/+NZSUnVvet3n7415GS6bZ3+YEgeTBQE2/W+zjdZ0Z3yHIwIDAQAB";
	public static final String base64EncodedPublicKey10 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmY1jgD+YBoK7e6E6cIzDr5mmesvGWVvf2dT7zuQWU8O4SSTHfXtAa4OJ9sIvIk6ZOlJOkc79w/qADO4KvTBVH05cJrxA7X6st57Y4+QREMzX/aX4V031eWlMbtwm596H/gGy0wUZS0TzednvpjxZujJ1mLhUQeciP/CglJXvOV4kqfxSbSGfYA7PEQ2NpQNOfhm6XquM+HxmpDKE54EK88q/HXJaxWwCiQ+9mbMubImWBxnc/0++bKTc0+6uXF0LBn5nJn9CnicGF2aAvqaFQ1XTTonw5icLEjEx/xxsyCe8kxPZSSUk42QsYp9IDZ37k8ndW2UrLR/xU/bNpfQ2nQIDAQAB";
	public static final String base64EncodedPublicKey11 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq+2xL/88vXBLw1Mq36TNSRWUL6QCxv9Olaa/FROGAAszhHP4SggI2Ks/ssQRFDeXKe906brHYc3dEZ1Hz6ufwJlW3mNyHUONS2/8oP1/P8VA/+gwjycg4QLaQ5hY2iOcR3eiClxVqoguRG6LUibdyLiKC/8ONOk6uOVdNd5zvS2aPlMXCB/Dge+HGtDrBMbUJDKRqq76kpfF4Azbq1nAsSyRJZsZqGhYPwBOYwelK1858ywo/E3qv6FH/34Ci07dE3XVKfl9KPI7yazIJmyhy9Ut3+22WfDOKrjZameX2T8GwQhjKOIsdnqEthFBn6xKHa4cqpnI2Ay48fAj7p20IQIDAQAB";
	public static final String base64EncodedPublicKey12 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApsuRENPETuiXOTrYZ3n3Hqo44okSqW+3GY3o7uABGeUSv4XIsjP1EOYEJLMN4W9hi5n+MAIwbtP99nyIxlSzAHyntthR9svp51ZXWEFiqLr9gGonbwtCgjzdtWOnd8oR9abySm1/vNGEAtMyE52YFnIZFQHZVanYdVrgYV+c6VhoLzI4vnqKOqwb0R5zfRted/EwJp3ucSykDwUWlToYxmoU8WBg6wfeGrgJnU2zSBG9VBy0ha3uKrdSGfMwqOfnnNRCUhVppEeAjtOjah7WVdCiEzD0nL9ODbCm46X+WRQm+AJVbslnB3Ovx1FANOoHCRrhgmWaDhPENa7/E+yoEQIDAQAB";
	public static final String base64EncodedPublicKey13 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm+bOcbCBqlNEeGGTDrS6eL9GAsVMgzeLenH7P4/MkOiSyssNjuAR1XpmOBQQe8M51qby0K+yGOk9yj/iPvGmE3p/Hd2erSG/uRDeiY0HleY9U+1B4phIp7XoXWkbSpzQw7VjqJPcEdKjl8kbu4J25gRLqY5QGv+5fE4BTu4a/wFYm9Lz5CX+gCnrGlQJzbz7AmQAfXJX1fHnknLV3ZXb98S4S0OmXD9ip4FuVzeCRYK1TxlgUt3lZew+71c78BB6hDoJB7riwOe8aDZqjAAh4XXHhwNMEhxWYuYihSsbBicVKBY7IVWXG4+CTAjzD7jznelUjnbQW77kPYYx2jE7OQIDAQAB";
	public static final String base64EncodedPublicKey14 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxkNSi310L/Byk6wieCGeVywaMR8dIaBgqKQsMHZ5XKb1wDi7xcF+2s2L32CQvpC4yq4o35vSpvx2c8JbxwuZCSKe43A1+Yj4NZp/TZmzT24QlWAlEGdQRXpb5gsiBGgMQQOVgvO2t7aHeVMMpzF+/QfthrRaRO7b16/e+wpFROZ5I9ycXWV8gDou3BgAtqZB2zYJLU5wSgym9GxybXAjXmvF1dXj8YF1tu/5rJOK9Qa1C26tqheIffQmpJtZaPLswu18Uhfj4o6rj10x9XsFL9CVzas7PN1x9SILYe7HkQDL7EuBcHZJ6F9DXhrV+j6I+IPDYrMvoNAoRWWLXXfgXwIDAQAB";
	public static final String base64EncodedPublicKey15 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjaCd1h28gj9tjcCbAj3UWfw2KIBj3qANSvebUlhH4CLzqgREzyAlURrJAh7Srnl2qrOpd6YSYowp4zm44xoTO8xLHmU0ma+v62x4frMKcTTjmqxWZ22ulcH0zwSjsuf5b7bvFwo+xhTPvSSMKYHkTn0wRp9PlXT9qu1QgHcKcUuWG9TrXiRQWeFXR7uaTjxEYXzxuGCV2TYBlSTqDWjb7vtWEzJuFeGc00QJezGUZsSdjLFIbS2AyqmIHTJF1hcbmIPr8hj682m3K6OFu4i5lqqR8iezR7Lim4jKNRz+Cepseahb3NE6Be59JyP1s19+AnIYGpkpkK4Z2uyD+cvIxwIDAQAB";
	public static final String base64EncodedPublicKey16 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn3JHiL91py0m1dYX5ze3H8T3dDIJqfs9i13hsY2QmwVGDLP2PbZ0IegFwc6VzpRnCvb8AWUhylJqW1Sq5a9a78ZWsxUpnO/XobmEGvl/BthohmiWl1kr8jUazd2guHfZh/rHDWqh/IjlSZ7QDfmaeqCmuQGKfy4nLU2gaPtO1Rz6Os04sBbs/0xW8bf39vf1AeG1iIIxuDo5BJBe62L6FmVXUusJKo78dowrI9ON5hRVnfolRClU+cGBnAP09cR5hCRLbmfHpTK2lHqPmUDzN2QXvqhKPm3eEt7wkoNOGQ+j4klRXgJ9HJaVRlsYZeKfproENwkY1EzlzTupfSlZKwIDAQAB";
	public static final String base64EncodedPublicKey17 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxQAsX/HxxZrxkrvS0W5tcp/DP9ukOG2W/p0xvze2IErHCcS8bQvQA5BA2z0qsFLBDnD1ZF4UtSur33cI1jGRD9lPCKiYNc/ot4NdQYjfYVuYDDy+sNnkky9itIeW7cUUi7aw6QReeFZ+BZEwR9Zo7evWJjgd3F4xe37fpEbRqCbtMH5mUQkqEGMX1j4gmAi6wbX/mmb3T1flkU8jW1xXNU9HhG39yXqLYFcLfaHc1Di4FoeI/lASUpmmV/DQB1R/IO9myi1nLlJyC4N1b/MxULLppaV6Wz3kmpKYw4YTNuW0az2Ruvobe/woE0UXSrhgzs0VXEM0Dr3M8YGrZcyy9QIDAQAB";
	public static final String base64EncodedPublicKey18 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmfSLnv5IRHDJh7CQhCxQxiMUu1wFVqo6i1z90F2AQUMhNWcXCDR9SqkxCGfXIdPGpPfe4sWm+yIH5liBEKN1+ulzxJQNuHRXAvkLA5KXVbDFFSSPHdXAqvuhip94YCP/+ccIm95SgIWYgh3EFA5vvvfU7m2OShtLdIBMbhUlVvZMp4nT1GADfhcSgcik7WUd2mJjY35M0O0JYnZRV1V1asGAWWsEMt/EhWqoyX5E4xKDKOV0Mb8xwo2raUKg/DW+YKgOns6DubR5miBxVaqZ4Z+dqHClVd7m8NbuFlF4C2eg69ZtTyQi3pYb8zr8c5fnZN8dQmrZOnSEn+NXZutMOQIDAQAB";
	public static final String base64EncodedPublicKey19 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAznXjN5ND6D1ribm7GXykRe1ZCoawXNNY62ju5Uc/fMmV3sfHs91b0m/f7HWsWGW2UkBanmNMsgiaM6fZbvdFdMdcQIWVLcVvgfeuUqUnGZ9HxuMIXv+fGd0QtJ0Pp6oG3RPfG+TB+sRuKtL13/as7qWXXE6ppYDaePh6IffRISLIaWh8au3KZ5iCfdv9Qo61PnKlu8oBcBfvimTblMdqXhdjGoIs5v+R7qEzBrgLqvr/ksPRwebxZz+meFbM0s6wQ1OPPEUimAMr/PLIXKX2+Z1o6yzxRg+8CJi8Rwhcg7wNYlyjLO80cpMBGDZ/gmPvwvNEjbeEZjSGGusgz309dwIDAQAB";
	public static final String base64EncodedPublicKey20 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwdLJ6AtkUnGSj16YdEwRKi6ig1rzw2j9UnJvyMcL2VjC3vMva3HvzxoOzG0LvsFMQd5pC4hsjLb/NmaasFvGENx3rbB+NUCuPfoyOds/nnca0UoVqC3ZQz+lK4sUq/UdeSGiJ3SXOs1yf6xbeMBkyU/5UCAOd9AeVe7+WDqKOWPnKj+cSVQHkKcWTAwnRnC6KSd31f8uz/0fzgKdIZhAFojs9pa2x+f5ZROx4aATSugeThSWjsg3bjN8/xtPcJFuoD3Kibza8ZgvA9QPIPBTCXYS+HJSKHnwgg1tYclhV9QUIh0cFISpoKUnb/0p+Eb6+MHa26Q02EE7jRFrlynNJwIDAQAB";
	public static final String base64EncodedPublicKey21 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhO8+ZGlB7UZbKhzuVTDQCSoHbspIH2YmSpp25hvPUh1W/sGpDPB7818829pp5vUuo1q8TLwXtikx+j4wi7iroDAtjr/XhanXQLpvEcVDRIYUK2P/tzQkQhJQViAV0z7J8io3tX6+TVv5qd7uEmMtDl5my9axoRdG/ODiKjyRlEOpS/wR+R/0385fGDvk7kRHgg1nqNv6LVvUikM6wqVSQkC+uywKjaBdoLpTHNYioivOh34ANIkBwHz0y9RmVsSoIBv4lmy6/iVS0d6qvCI0VTKw8Qy90ACYvyqN8OcVPOtLQXLAwFdXj16bQ69on/DPrt+xCaJTX+XixZ3KRwqVYQIDAQAB";
	public static final String base64EncodedPublicKey22 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk4AR30J2XNQrmypmXjB6YtyDYaXR0hAW36TwS4JYh1HqF2ZyremrwJ4K0+TcTklR5xPk8h8kvvc2bNcVFk9KctH6E5RGeRz/Dd22vKVp9EltuorcveTRSBtMKp9y2ooDis0ZvA2VMuz3jIHnyW97r6SGI134nislueCgIqiGV3gTMGV6u8ih2tNtlvB7Cl+odX6q9KOVdrw01xY4cMaAMw1G9mYFgALBnjef1NCnFfLUlNJR/UzPbLYFwabGTt5lzMY/jClCb8p27RyFnrh/LqORHi+q+F5jxvDYyJT7qdi5bUK2kQZocwUJUSEeaXeNqPT19J2JeB8CUQQaAjQ91QIDAQAB";
	public static final String base64EncodedPublicKey23 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl3Bk6ypGdzYYxewiZNnAqIZsl6B9VgkxEB26SOUguZL2iEHPOdTrKbTO/CbRaNwrX6RHSiuBySpTV7+lrQcaUPbtAETFOEHgCSLjMAVBY2oIkGiXXiu7R3l/gda6ZgH52zLkoGSuRejcuvkhfPvQFB0wxCMnbSrhWsG6pzV8Eg9IRIcUHBffv4MZdWQp7wsDraTzd3vGy+dy168c1CXPRl8qoKV459kaNOkoQFH7u98Rf0qd/mLPzmgU7hKs6ACSizSIihpE7tuqdoRk6h8hFeA0xnvv+wSmVOX2g0a4QMXQPZmpE03ChnNbQp1ZlpuUGf+/HkGyf5CC4cbkdDkbcQIDAQAB";
	public static final String base64EncodedPublicKey24 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgvFQs272RVcPRiCBIoQco+6vIOqlwMfeqjoEUMSdRWN0xYPE11R3h3IVjb6EUks4wLryBn64kyVKqD8Bg63hLk9Nz7V4LlesIO5P2rJ0rLKs/B9BeH2TyUd81vh6HlFztZHFdBDrOlKjhFBqceTNL/LRDBOVct72QsGc/aEFUKAmXLDvqEBZwBsA/u3aZsAo/bCA1SGJLTyhXQQHvezgEKbO/N9MQjQUO0dOhQ5wfqylPlpDCaHE0wNzPJBxWRjbLu7XbljvfZVfVerYO1h5k2OQL4ctQS2elaVTem7M+mL0yys/zL2891DD7lMO5xEnJUJVPQt28YIoMlxhniBVEQIDAQAB";
	public static final String base64EncodedPublicKey25 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAolLt40RDaPGoLiVZ+o2tJqbP3ySzqUdZ0hDhCNiHcjhMPGfka7F8w2EfdOs8v3onE5cQs5W++q8go3oH/fd3gx2uMfBYdMrv29fWprkuHAS8mYrFmbol46fv9D+Inh5I0DQumODn4kmA+ei7Gjj2Os4KG9zPdYRtGhpZPrK55p/f0Ndwa9C3W5Gp9YSNllb4zb3ORauY7PGYaRnjapSWPqO++ugitg1lROGVn04goE6FKb/AbCoXDtBYghV3r7+mV/SdKjFwh3qQRaRMllDnjLo40uk3CRjxcG2QFVjIixpswPkSFzQqZcYgK3xc86Qa/jKq4rlFvmpQXkKKNiDh7wIDAQAB";
	
	public static final String base64EncodedPublicKey26 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqwaPI/EgSdljjHjtzoDSfaN25rCRJdkPcvIAks69ij1ZGYs5yBi4UwiaAzDOP5cjhsAemHlyvyBxfHFjtsfOVIcSlZCmizEi6+/aou2PTft86NkhT6AI5UQP9nCF43MixK3nvUrZgZWUc4xHH9en3TVorlwFBMvXTr9LiiKuo4VuY/EZtF18Lqfn6sYUddonaaQQRZvOcwW9w0onWbnLU3BSG/Fqfh4VFg+DX17Kcwt7gpJGQJYS1CJZDPm3ew+cqAE5BPybx92CRFMB9+lXaCWEXing1UYNTfXrmm44JTe/BsQeXbxrlEGYSNOKrycMGiQk4g4e99hYoZF1tnIKcwIDAQAB";
	public static final String base64EncodedPublicKey27 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvpEuwDFHWDZT+rKNcMPcnv9680edmY0llbw7GdXY0TKxpxyHPoyeht6a3M0fxJsteRbU0wodTed0valVi3AKbtJsTAZMdpT0kTVT4p1sHcxF8+/zswqIR//89e3ldtMZQdnivTeTxS/YokH5hZZ4xTOuezveE3ZNs+EFRuRH/lZG3Ja0x19rl8jFxp+ravAoJ5P1n1R3xNo80r2EZui8HJFnYxrKWVYv83DGbHLzNQiyo2KM6mJbpI5PBBmJY+7oPm7AbknSqAp7jqyzFBWtTFLk0/ca+ZFxJD9XRtMuLCRdqKt3yqDqWQvwYWdwWe2CurO6ejzFjip35jqXI3w3TQIDAQAB";
	public static final String base64EncodedPublicKey28 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjL2duDNEMzvsY4TYiicbCEd+XCHuYDaN9CDivOYpYsiWcBMnB4hOA4YSOGHM4Tv6gcQsb36RrXw1CDvBv4LOmZ3l1MqC5GouTgtN1nYuBIA9KaHEX6vGNy6fZGV5TeHINO7OK51ZQJQg/Zo7+fSuCEt5czGMwmMqUCC6YiUi/VSUgrbWZRnICYjiMfhceDcAXDmh+FTUVcLXoCWJbpB0ntnG9MrXNiz9kx7LBmsZpgf1tPf+GgsknENa0USErSzjZOTNAWtLxhU9j5BeJ+yt92/1nGoqTLbEUm+79p+hXFkrVTNBiZm6ZiqzRjN+C5UfTvNRv0qY/t2etQwNWA6MUQIDAQAB";
	public static final String base64EncodedPublicKey29 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg6qgS6ezIdXhKqZxgqjmY8hvF7bzuV2F0Iggrt3abpecFTtLDHTApYCuF/8BIiRmO1MuVL8ueoaxEgk6Xg4K4+l0Oak3lUwSawJIdZEvxva2a/Q01OJPdoslNKSDO7IEe4c4nCE3Vq3S5rvBnlVwfGOuuHKraWBKgvWWUjisTUmlBRu8mfmHJvz6GhwB9putZYlnhb3y2iLCUUJacgr1nOaGrzzuh/q1mf315VTxeOC2R8NDQASvnofd7TCb9h26aFXxroumeOL8V1MgYUaViqobx2prsmW3gjXXTRT5wO8fcAH9HqYWwgcn2RJskBC6llxlJvKgZUoqIG9eu4Dh8QIDAQAB";
	public static final String base64EncodedPublicKey30 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiYoe6pXsL8bijspTRqbzCbb42nGmEU7n6n78derbnI7hbMj/ATvG1Uj5npmPUX0RSjXqbX8PSkzmhnQglf22bf+6I8VgUX/Mvg2BERJDV4IrlgkH+87c3MzUviM6cwW//8FTr1RVjHg7vd4Oo60ELxNyKeVfykCYgWXfrYizZDudgYQh6S2Ux4glCIcsBh3VVYZoy/uNYomzY7AWcASDPO7OpkAYQapBbBT7iX/+81GyM+xs2QmBkHy9dQnEvIIDZggOvAybR6iY4c0oSccEKb0SI4L4d1cpmIRZlLkAnD8Fneio3gblQXsvt+z3TpXWiRsbsdCH2WzMyz/CunGWcwIDAQAB";
	public static final String base64EncodedPublicKey31 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmCKBB+R3/vwDjCCt7F8zL/f2pTkPLHDWJRdsYSoPD5ErYAzPplVGyFKNEwCy2K87vTK/c5z8GiR/9S9RrbFSo5q5o1GEClc5MN1fQP0XIiXbv1t/Cd4bDZ3yvL4dA6DaE7/XTvKr2JYD0HHxJX1ndjcn28aJ3f5I6zbmlrCxW13aMdaf1FoCjUD0wJp9VGY6U9OLP9BQcDjtkxCuQAef/gLh5xfglaC/frSey26cgtokNIArAnIsTetvlEP9liBew+jgQrdmmoPhqQT4gkQiEDC7CsHfd5AZnvB2Vaa6GDicQa6nwY+847+f27aMh5XNO1IlJLDLH3qRcGiuTrMqTwIDAQAB";
	public static final String base64EncodedPublicKey32 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiKw/XmdtpYdj5kvC8Fw+KRUptEtSaabfVwrWONXV61vYsNpKDaBn8lWxVXD5i3QdmlZ+Ob6RvaRaZbtx4fGyDV5EOCM0xKxvGypupM7s08uxm5st9ncu7arHfW9NqtGEe4TezSlhYoFqI+5ZjSrr/sxlmuqYNTaCWllkR/B9uWhZLlA5daP3k/8N8wzpAAGlRCsfiQ/AskN8qQ7JljWmKxgfz4ozcl3zQ88jx4LUi+VAYsF+Z1WzABvmigRyTejpJK6NyoIvbiXzxHyMRl6KwA54Ba5uuv8QmXh6zNrvPNVBbKK7aTMU3xNDAIfzKP1AIpYcsbC14XGZebJRgvSfOwIDAQAB";
	public static final String base64EncodedPublicKey33 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnSOg8K/TS+3eKSGT+4He8yyS/ljN5zUS6skSGdBGHqUYhtXgsipKkWgtZta3E7klm3P9Wc4ubqjWOfYbnITNZGjVRsvsTM5ufbNxF0wqUeFC5jFPwcAuMK9kk0t1bYdGN4KSm3LKKhRIW5wnETzHPs0DNR9SkabOW5li1KDWvIquHyFjnYu4KPhwwPQlbMjGjEL6RtafPUSEkz5gO2GSun0HPaI6hCAUFFrS91Oc0tWRs7uLvwW3QrPRZtiVd62M8k5v/iSu6vFNW9OZVWANWsvTzAZ+gZNb/HVhva50DjazVETOOuQNM4sye30nxA7EvAbC9yOMKv/99zRViQhS4QIDAQAB";
//	private static final String TAG = "IABUtil/Security";

	private static final String KEY_FACTORY_ALGORITHM = "RSA";
	private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

	private static final String base64EndcodePublicKeyIndia1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkaclSmw8EBYJNJCAUUj/McA4aICHf/jYUR0RWo7sJhcvQmRTJ6at+wzZKEnbC9W7LwxQBlpGiV61gNrX/05EtPLatRnkxTDoNbCJa7f1pFZbQ+zHGJMo496h6/Oh+mAyFrHew4vBQzqv/47dVccloa5ZyrB8clXRy3BUiaRGqMBPhQ6diZ3vfDKHBO4MMzBwwvSR8S84bTNlzgK8PaEcUUXEOU3VmqjsB6yRPTnu58W+EsH36VYaomBiwJEtW0HrYyDdHwj7BoW7Z57WrfQ6jlDOJjEMp6op2WwZvXqT4lBY3Twsa8MUXiji1EIk1FxJcfhdIcfHmU8mJUbEL7ZrlwIDAQAB";
	/**
	 * Verifies that the data was signed with the given signature, and returns the verified purchase. The data is in
	 * JSON format and signed with a private key. The data also contains the {@link PurchaseState} and product ID of the
	 * purchase.
	 * 
	 * @param base64PublicKey
	 *            the base64-encoded public key to use for verifying.
	 * @param signedData
	 *            the signed JSON string (signed, not encrypted)
	 * @param signature
	 *            the signature for the data, signed with the private key
	 */
	public static boolean verifyPurchase(String signedData, String signature, String pakname) {

		if (signedData == null) {
			System.out.println("data is null");
			return false;
		}

		boolean verified = false;
		if (signature != null && signature.length() > 0) {
			String publickey = base64EncodedPublicKey ;
			if (pakname.equals("pkgname: com.dautruong.dautruongcasino"))
				publickey = base64EncodedPublicKey33 ;
			else if (pakname.equals("com.gogogamenhatantat.studiovietdat"))
				publickey = base64EncodedPublicKey32 ;
			else if (pakname.equals("com.ione.doithuong.fun3c"))
				publickey = base64EncodedPublicKey31 ;
			else if (pakname.equals("gamebai.dautruong.doithuong.vn"))
				publickey = base64EncodedPublicKey30 ;
			else if (pakname.equals("com.g3c.gamebaidoithuong"))
				publickey = base64EncodedPublicKey29 ;
			else if (pakname.equals("org.gamebaidoithuong.choibai") || pakname.equals("net.gamebai.fundoithuong"))
				publickey = base64EncodedPublicKey28 ;
			else if (pakname.equals("doithuong.gamemienphi.vnn"))
				publickey = base64EncodedPublicKey27 ;
			else if (pakname.equals("xocdia.dangian.doithuong.mienphi"))
				publickey = base64EncodedPublicKey26 ;
			else if (pakname.equals("lieng3cay.gameso.com"))
				publickey = base64EncodedPublicKey1 ;
			else if (pakname.equals("com.tienlienmiennam.tienlendoithuong"))
				publickey = base64EncodedPublicKey2 ;
			else if (pakname.equals("com.danhbaidangcap.xocdia"))
				publickey = base64EncodedPublicKey4 ;
			else if (pakname.equals("gamebai.dautruong.thanbai"))
				publickey = base64EncodedPublicKey3 ;
			else if (pakname.equals("com.chan.samloc.game52fun"))
				publickey = base64EncodedPublicKey5 ;
			else if (pakname.equals("net.ongame68.danhbai"))
				publickey = base64EncodedPublicKey6 ;
			else if (pakname.equals("dautruongb52.vietdat.com"))
				publickey = base64EncodedPublicKey7 ;
			else if (pakname.equals("chanonline.gamevui.com"))
				publickey = base64EncodedPublicKey8 ;
			else if (pakname.equals("dung59.bigkool.gamebaidoithuong"))
				publickey = base64EncodedPublicKey9 ;
			else if (pakname.equals("vn.banchinhthuc3c.khongmatphidoithuong"))
				publickey = base64EncodedPublicKey10 ;
			else if (pakname.equals("com.tienlen.miennam.samloc"))
				publickey = base64EncodedPublicKey11 ;
			else if (pakname.equals("gamebai.iwin.langquat"))
				publickey = base64EncodedPublicKey12 ;
			else if (pakname.equals("com.chan.sam.lieng.gamebai"))
				publickey = base64EncodedPublicKey13 ;
			else if (pakname.equals("com.xocdiadoithuong.xuan2015"))
				publickey = base64EncodedPublicKey14 ;
			else if (pakname.equals("com.gamebai52Fun.xocdia"))
				publickey = base64EncodedPublicKey15 ;
			else if (pakname.equals("xocdiadoithuong.xocdiaonline.gamexocdia2"))
				publickey = base64EncodedPublicKey16 ;
			else if (pakname.equals("com.chan.langquat.dst"))
				publickey = base64EncodedPublicKey17 ;
			else if (pakname.equals("xito.lieng.doithedienthoai"))
				publickey = base64EncodedPublicKey18 ;
			else if (pakname.equals("com.dautruongonline.gamesamloc"))
				publickey = base64EncodedPublicKey19 ;
			else if (pakname.equals("trachanhquan3c.trachanh3c.gametrachanh3c"))
				publickey = base64EncodedPublicKey20 ;
			else if (pakname.equals("gamebaitop.doithuong.online"))
				publickey = base64EncodedPublicKey21 ;
			else if (pakname.equals("bai3cgame.baionline.com"))
				publickey = base64EncodedPublicKey22 ;
			else if (pakname.equals("gamebai3c.gamebaidoithuong.xocdiadoithuong.danhbai3cdoithuong.trochoixocdia"))
				publickey = base64EncodedPublicKey23 ;
			else if (pakname.equals("com.gamebai3c.game3c.xocdia3c"))
				publickey = base64EncodedPublicKey24 ;
			else if (pakname.equals("gamebaidoithuong.xocdiadoithuong.tienlenmiennamdoithuong.danhbaidoithuong"))
				publickey = base64EncodedPublicKey25 ;
			else if (pakname.equals("com.teenpattiarena.supreme2017"))
				publickey = base64EndcodePublicKeyIndia1 ;
			System.out.println("==>Key:" + pakname + "-" + publickey) ;
			PublicKey key = Security.generatePublicKey(publickey);
			verified = Security.verify(key, signedData, signature);
			if (!verified) {
				System.out.println("signature does not match data.");
				return false;
			} else
				return true ;
		}
		return false;
	}

	/**
	 * Generates a PublicKey instance from a string containing the Base64-encoded public key.
	 *
	 * @param encodedPublicKey
	 *            Base64-encoded public key
	 * @throws IllegalArgumentException
	 *             if encodedPublicKey is invalid
	 */
	public static PublicKey generatePublicKey(String encodedPublicKey) {
		try {
			byte[] decodedKey = Base64.decode(encodedPublicKey);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
			return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeySpecException e) {
//			Log.e(TAG, "Invalid key specification.");
			throw new IllegalArgumentException(e);
		} catch (Base64DecoderException e) {
//			Log.e(TAG, "Base64 decoding failed.");
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Verifies that the signature from the server matches the computed signature on the data. Returns true if the data
	 * is correctly signed.
	 *
	 * @param publicKey
	 *            public key associated with the developer account
	 * @param signedData
	 *            signed data from server
	 * @param signature
	 *            server signature
	 * @return true if the data and signature match
	 */
	public static boolean verify(PublicKey publicKey, String signedData, String signature) {
		Signature sig;
		try {
			sig = Signature.getInstance(SIGNATURE_ALGORITHM);
			sig.initVerify(publicKey);
			sig.update(signedData.getBytes());
			if (!sig.verify(Base64.decode(signature))) {
//				Log.e(TAG, "Signature verification failed.");
				return false;
			} else
				return true;
		} catch (NoSuchAlgorithmException e) {
//			Log.e(TAG, "NoSuchAlgorithmException.");
		} catch (InvalidKeyException e) {
//			Log.e(TAG, "Invalid key specification.");
		} catch (SignatureException e) {
//			Log.e(TAG, "Signature exception.");
		} catch (Base64DecoderException e) {
			e.printStackTrace();
//			System.out.println("==>ERROR allowJoin=>verify:" + e.getMessage());
		}
		return false;
	}
}
