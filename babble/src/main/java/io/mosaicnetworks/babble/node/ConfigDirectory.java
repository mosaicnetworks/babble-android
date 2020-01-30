/*
 * MIT License
 *
 * Copyright (c) 2018- Mosaic Networks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.mosaicnetworks.babble.node;

public final class ConfigDirectory {

    public final String directoryName;
    public final String appId;
    public final String uniqueId;
    public final String description;
    public final boolean isBackup;
    public final int BackUpVersion;

    /**
     * This constructor takes a directory name and parses it to populate the public final properties
     * of this class
     * @param directoryName the config directoryname
     * @throws IllegalArgumentException is thrown when the directoryname is malformed
     */
    public ConfigDirectory(String directoryName) throws IllegalArgumentException {
        this.directoryName = directoryName;

        String[] tempArray;
        String delimiter = "_";

        tempArray = directoryName.split(delimiter);

        if (tempArray.length < 3) {
            throw new IllegalArgumentException();
        }

        appId = tempArray[0];
        uniqueId = tempArray[1];
        description = DecodeDescription(tempArray[2]);

        if (tempArray.length < 4) {
            isBackup = false;
            BackUpVersion = 0;
            return;
        } else {
            int version;

            try {
                version = Integer.parseInt(tempArray[3]);
            } catch (Exception e) {
                isBackup = false;
                BackUpVersion = 0;
                return;
            }
            BackUpVersion = version;
            isBackup = true;
        }
    }

    /**
     * This function takes a config folder name and removes the backup extensions
     * @param compositeName is a config folder name
     * @return compositeName with the backup suffix elided
     * @throws IllegalArgumentException
     */
    public static String rootDirectoryName(String compositeName) throws IllegalArgumentException {

        String[] tempArray;
        String delimiter = "_";

        tempArray = compositeName.split(delimiter);

        if (tempArray.length < 3) {
            throw new IllegalArgumentException();
        }

        return tempArray[0]+"_"+tempArray[1]+"_"+tempArray[2]+"_";
    }


    /**
     * Encodes the description to make it filename safe. Spaces become minus signs, all other
     * non-alphanumeric characters are striped
     * @param description the text to be encoded
     * @return the encoded version of description
     */
    public static String EncodeDescription(String description) {
        return description.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll(" ", "-");
    }

    /**
     * Decodes the description from the filename safe version.
     * @param description the text to be decoded
     * @return the decoded human-readble version of description
     */
    public static String DecodeDescription(String description) {
        return description.replaceAll("-", " ");
    }
}
