package com.github.maracas.rest;

import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("com.github.maracas.rest")
@ExcludeTags("slow")
public class MaracasRestTestSuite {

}
