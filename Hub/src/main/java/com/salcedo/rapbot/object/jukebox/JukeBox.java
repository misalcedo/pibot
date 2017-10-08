package com.salcedo.rapbot.object.jukebox;

import java.nio.file.Path;

public interface JukeBox {
    Path next();

    Path previous();
}
