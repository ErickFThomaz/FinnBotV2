package br.dev.erickfthz.finn;

import br.dev.erickfthz.finn.core.FinnCore;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    @Getter
    private static FinnCore finnCore;

    public static void main(String[] args) {
        logger.info("Iniciando o bot...");
        finnCore = new FinnCore();

        finnCore.inicialize();
    }
}