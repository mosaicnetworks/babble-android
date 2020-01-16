package io.mosaicnetworks.babble.node;

public class ConfigFolder {

    public final String FolderName;
    public final String AppId;
    public final String UniqueId;
    public final String Description;
    public final boolean IsBackup;
    public final int BackUpVersion;


    /**
     * This constructor takes a folder name and parses it to populate the public final properties
     * of this class
     * @param folderName the config foldername
     * @throws IllegalArgumentException is thrown when the foldername is malformed
     */
    public ConfigFolder(String folderName) throws IllegalArgumentException {
        this.FolderName = folderName;

        String[] tempArray;
        String delimiter = "_";

        tempArray = folderName.split(delimiter);

        if (tempArray.length < 3) {
            throw new IllegalArgumentException();
        }

        this.AppId = tempArray[0];
        this.UniqueId = tempArray[1];
        this.Description = DecodeDescription(tempArray[2]);



        if (tempArray.length < 4) {
            this.IsBackup = false;
            this.BackUpVersion = 0;
            return;
        } else {
            int version;

            try {
                version = Integer.parseInt(tempArray[3]);
            } catch (Exception e) {
                this.IsBackup = false;
                this.BackUpVersion = 0;
                return ;
            }
            this.BackUpVersion = version;
            this.IsBackup = true;
        }
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
