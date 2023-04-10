package com.zzj.util.filewatcher;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Watcher {

    private String file;

    public Watcher(String file) {
        this.file = file;
    }

    public abstract void handle();
}
