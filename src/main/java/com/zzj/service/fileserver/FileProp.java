package com.zzj.service.fileserver;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileProp {
    private String name;

    private String lastModified;

    private String length;

    private String path;

    private String style;
}
