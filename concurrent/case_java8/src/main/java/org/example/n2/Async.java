package org.example.n2;

import org.example.Constants;
import org.example.n2.util.FileReader;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = "c.Async")
public class Async {

    public static void main(String[] args) {
        new Thread(() -> FileReader.read(Constants.MP4_FULL_PATH)).start();
        log.debug("do other things ...");
    }

}
