﻿#The MOGLi Code Creator   

* * *

Just another code generator? - NO! - It's THE Lightweight autogeneration tool! 

It's provides inserting into existing files, making complete new files and building trees of files within the file system.

It's a small standalone Tool for a quick start into model based development!

It's easy to learn, to apply and to integrate in your IDE.

It's quick in execution.

It's failsafe because it supports Reverse Engineering: re-generate your artefacts as your wish.

It's written in Java but made to generate all kinds of text documents.

* * *

**MOGLi** stands for the following attributes: 

**M**  odel based

**O**  pen for extension

**G**  enerator based

**Li**  ghtweight  


* * *

Copyright by [IKS GmbH](https://www.iks-gmbh.com)

Licenced under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

travis-ci: [![Build Status](https://travis-ci.org/iks-github/MOGLiCodeCreator.png?branch=master)](https://travis-ci.org/iks-github/MOGLiCodeCreator)

Current version: **1.6.2**

**Changes to 1.6.1**
- Bug:         The preparation run was troublesome mechanism to check whether model and template match, and was replaced in all three generators.
- Improvement: Documentation of the template utilities and the meta model of the StandardModelProvider now comes as JavaDocs.

**Changes to 1.6.0**
- Bug:         It's possible now to share subtempates between generator plugins.
- Improvement: It's possible now to use customized static utilities via a dropin mechanism.
- Improvement: New artefact property 'CheckMissingMetaInfos' for all generator plugins
- Improvement: New TemplateJavaUtility method 'throwMOGLiCCException'

**Changes to 1.5.5**
- Improvement: Licensed under Apache License 2.0
- Improvement: NEW fun feature: New templates to generate console comic strips and the model file Moglydick that uses these templates
- Improvement: Help files strongly improved
- Improvement: New model file ShoppingCart and templates XMLBuilder to demonstrate the LineInserter features
- Improvement: new TemplateStringUtility method getTextFileContent(String file)
- Improvement: new features of the ModelParser to parse variables
- Improvement: Performance improvement
- Improvement: Number Sign Replacement
- Improvement: Java special solution <package> replaced by general solution


* * *


Versioning convention: major.minor.revision

major:    will change for basic framework modification

minor:    will change for new features and larger improvements

revision: will change for bug fixes and smaller improvements


* * *


####Markdown Documentation

you can find documentation around markdown here:
- [Daring Fireball] [1]
- [Wikipedia - markdown] [2]

  [1]: http://daringfireball.net/projects/markdown/syntax
  [2]: http://en.wikipedia.org/wiki/Markdown
