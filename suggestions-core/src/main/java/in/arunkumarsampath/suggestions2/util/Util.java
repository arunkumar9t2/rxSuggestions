/*
 * Copyright 2018 Arunkumar
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

package in.arunkumarsampath.suggestions2.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Util {

    private Util() {
    }

    private final static String UTF8 = "UTF-8";
    private final static String ISO = "ISO-8859-1";

    @NonNull
    public static String prepareSearchTerm(@NonNull String searchTerm) {
        return searchTerm.trim().replace(" ", "+");
    }


    /**
     * Tries to extract type of encoding for the given content type.
     *
     * @param contentType Content type gotten from {@link java.net.HttpURLConnection#getContentType()}
     * @return Extracted encoding.
     */
    @NonNull
    public static String extractEncoding(@Nullable String contentType) {
        final String[] values;
        if (contentType != null) {
            values = contentType.split(";");
        } else {
            values = new String[0];
        }
        String charset = "";

        for (String value : values) {
            value = value.trim().toLowerCase();
            if (value.startsWith("charset="))
                charset = value.substring("charset=".length());
        }
        // http1.1 says ISO-8859-1 is the default charset
        if (charset.length() == 0)
            charset = ISO;
        return charset;
    }
}
