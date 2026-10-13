# IFT3913 — Tâche 2 : augmentation des tests de Tika

| Nom complet | Identifiant GitHub |
|---|---|
| Rayen Rizk | [rayenrizk](https://github.com/rayenrizk) |
| Mohamed Badr Ouammou | [gy23578](https://github.com/gy23578) |

**Dépôt du travail :** [rayenrizk/tika](https://github.com/rayenrizk/tika)  
**README de remise (à placer à la racine du dépôt) :** [README.md](https://github.com/rayenrizk/tika/blob/main/README.md)  
**Branche :** `main`  
**État vérifié :** code et workflow du 10 octobre 2026 ; quatre rapports PIT reproduits le 12 octobre 2026 ; comparaison XML contrôlée le 13 octobre 2026  
**Classe étudiée :** `org.apache.tika.io.FilenameUtils`, dans `tika-core`  
**Preuve d'exécution des tests :** [GitHub Actions — succès, 10 octobre 2026](https://github.com/rayenrizk/tika/actions/runs/38023804518)

## Objectif

Nous avons utilisé ChatUniTest avec un modèle local pour proposer des tests pour `FilenameUtils`. Nous avons vérifié et corrigé les propositions, puis utilisé PIT pour mesurer l’effet des tests générés et des tests manuels sur la détection de mutants.


### Résumé des résultats et traçabilité

| Élément | Résultat | Justificatif |
|---|---|---|
| Classe de production | `FilenameUtils` du module autorisé `tika-core` | Code et suite d'origine liés ci-dessous |
| ChatUniTest | Greffon Maven `2.1.1` avec Ollama en local | `tika-core/pom.xml` et historique de génération documenté |
| Tests IA retenus | 17 (11 + 6) après correction des oracles et de la compilation | Deux classes JUnit versionnées |
| Tests manuels | 14, ciblés sur les métadonnées et les frontières | `FilenameUtilsMutationTest.java` |
| Mutation initiale / finale | 73/115 (63,48 %) → 94/115 (81,74 %) | [Quatre rapports HTML/XML archivés](docs/pit-reports/01-originaux/index.html) et comparaison par identifiants des 115 mutations |
| CI des tests ajoutés | 31 tests, 0 échec, 0 erreur, 0 ignoré | [Run GitHub Actions vérifié](https://github.com/rayenrizk/tika/actions/runs/38023804518) |

**Portée et niveau de preuve.** Les fichiers Java, la configuration Maven et la réussite des **31 tests dans GitHub Actions** ont été contrôlés sur le dépôt publié. Les **quatre rapports PIT (HTML et XML)** ont ensuite été générés localement et joints au paquet de remise. Ils établissent les quatre scores, les **21 mutants nouvellement tués**, leurs transitions exactes et les tests tueurs indiqués par PIT. Le dernier rapport donne **94/115 tués**, **6 survivants**, **15 non couverts**, **160/175 lignes couvertes** et **94/100 en test strength**. Les fichiers ne deviendront consultables sur GitHub qu’après leur publication.

## 1. Choix de la classe

Nous avons choisi une classe du module autorisé `tika-core` : `org.apache.tika.io.FilenameUtils`. Elle traite notamment l’extraction de noms de fichiers, le nettoyage de noms et de chemins embarqués, les métadonnées et les limites de longueur.

La classe possédait déjà des tests, dont `FilenameUtilsTest.java`, mais sa couverture était incomplète. Dans la mesure PIT de départ, 22 mutants n’étaient pas couverts et 20 mutants survivaient. Nous avons donc choisi une classe testée, mais pour laquelle PIT révélait encore des comportements à vérifier. Les essais de génération ont porté notamment sur `getName` et `getSanitizedEmbeddedFileName`; les tests manuels ont aussi ciblé les replis de métadonnées, les chemins et les limites.

- [Code de `FilenameUtils`](tika-core/src/main/java/org/apache/tika/io/FilenameUtils.java)
- [Tests originaux de `FilenameUtils`](tika-core/src/test/java/org/apache/tika/io/FilenameUtilsTest.java)

Nous analysons une classe de production et ajoutons trois classes de tests.

## 2. ChatUniTest et modèle local

| Outil | Version ou configuration |
|---|---|
| Java | Amazon Corretto 17.0.17 |
| Maven | 3.9.16 |
| ChatUniTest | 2.1.1, appelé comme objectif Maven |
| Ollama | 0.35.1 |
| Modèle local | Qwen2.5-Coder 3B (`qwen2.5-coder:3b`) |
| Adresse locale | `http://localhost:11434/v1/chat/completions` |
| PIT | 1.30.0, avec le greffon JUnit 5 1.2.3 |

ChatUniTest a été lancé depuis Maven avec le goal `io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:method`. Le greffon est bien déclaré dans le `tika-core/pom.xml`. **Correspondance des modèles vérifiée :** le fichier `pom.xml` utilise `<model>codeqwen:v1.5-chat</model>` et le journal de travail identifie `qwen2.5-coder:3b`. La commande `ollama list` du 12 octobre 2026 montre que ces deux étiquettes partagent **le même ID local `f72c60cabf62`** et une taille de **1,9 Go**. Il s'agit donc de deux noms pour le même modèle installé sur ce poste ; le `pom.xml` peut conserver l'alias `codeqwen:v1.5-chat` accepté par ChatUniTest. ChatUniTest n'a pas été lancé dans GitHub Actions : cette génération dépend du serveur Ollama local. GitHub Actions exécute les tests Java déjà produits et intégrés au dépôt.

Exemple de commande utilisée pour générer des tests de `getName` :

```powershell
mvn -pl tika-core test-compile io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:method "-DselectMethod=org.apache.tika.io.FilenameUtils#getName"
```

Le nom complet de la classe dans `selectMethod` est nécessaire. Le paramètre `prune` est présent dans le `pom.xml` publié mais a produit un avertissement de non-reconnaissance lors de certains essais locaux; nos conclusions ne dépendent pas de ce paramètre.


### Installation et reproductibilité

Les deux greffons sont déclarés dans `tika-core/pom.xml` :

```text
io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1
org.pitest:pitest-maven:1.30.0
org.pitest:pitest-junit5-plugin:1.2.3
```

L'API compatible OpenAI fournie localement par Ollama était appelée à l'adresse `http://localhost:11434/v1/chat/completions`. Cette adresse n'est **pas** une API distante payante : elle désigne le service lancé sur l'ordinateur de génération. Les tests retenus, une fois enregistrés en Java, ne dépendent plus d'Ollama pour s'exécuter dans la CI.

L'identité de l'alias a été **vérifiée localement** : `ollama list` retourne `codeqwen:v1.5-chat` et `qwen2.5-coder:3b` avec **le même identifiant `f72c60cabf62`** (taille 1,9 Go). Le premier nom est celui configuré dans `pom.xml` pour ChatUniTest ; le second est la désignation du modèle sous-jacent. Sur une autre machine, il faut installer `qwen2.5-coder:3b` et créer l'alias `ollama cp qwen2.5-coder:3b codeqwen:v1.5-chat` si cet alias n'existe pas. Les identifiants locaux peuvent varier selon les versions du modèle.

Commandes utilisées ou utiles pour vérifier la configuration locale :

```powershell
ollama list
ollama show codeqwen:v1.5-chat
ollama show qwen2.5-coder:3b
```

La commande Maven de génération s'appuie sur le `pom.xml`; les essais pour la classe entière peuvent aussi se lancer avec le but `:class` et `-DselectClass=org.apache.tika.io.FilenameUtils`. Les résultats retenus ont été **sélectionnés, relus et corrigés**, et non présentés comme des productions directement utilisables sans intervention.

## 3. Tests générés

Les fichiers de tests conservés sont dans `tika-core/src/test/java/org/apache/tika/io/` :

| Fichier | Contenu | Nombre de tests |
|---|---|---:|
| [`FilenameUtilsGeneratedTest.java`](tika-core/src/test/java/org/apache/tika/io/FilenameUtilsGeneratedTest.java) | Tests proposés pour `getName`, puis relus et corrigés | 11 |
| [`FilenameUtilsSanitizedGeneratedTest.java`](tika-core/src/test/java/org/apache/tika/io/FilenameUtilsSanitizedGeneratedTest.java) | Cas générés pour les noms embarqués, puis adaptés | 6 |
| [`FilenameUtilsMutationTest.java`](tika-core/src/test/java/org/apache/tika/io/FilenameUtilsMutationTest.java) | Tests écrits manuellement à partir des résultats PIT | 14 |
| **Total ajouté** |  | **31** |

Pendant les essais locaux, les sorties brutes de ChatUniTest étaient produites sous `tika-core/chatunitest/tika-parent/tika-core/` et les historiques de génération sous `tika-core/target/chatunitest-info/tika-parent/tika-core/`. **Ces répertoires ne sont pas actuellement versionnés sur GitHub**; seul le code Java retenu est consultable dans le dépôt distant. Les sorties de `target/` sont temporaires et peuvent être remplacées par une nouvelle exécution Maven.

Lors d'une phase exploratoire, **55 propositions** avaient été produites dans plusieurs fichiers (notamment pour `normalize`, `getName`, `getSuffixFromPath`, `getSanitizedEmbeddedFilePath` et `calculateExtension`). Une première exécution exploratoire avait donné **19 réussites et 36 échecs**. Ces chiffres décrivent les propositions brutes et **ne doivent pas être confondus** avec les **17 tests générés retenus et corrigés**, ni avec les **31 tests finaux** (17 issus de l'IA + 14 écrits manuellement).


### Inventaire détaillé des 17 tests retenus à partir de l'IA

Les 11 cas dans `FilenameUtilsGeneratedTest.java` portent sur le résultat exact de `getName`. Ils sont explicitement assertés avec `assertEquals`, ce qui rend l'oracle plus précis qu'une simple assertion « le résultat n'est pas nul ».

| Test JUnit | Entrée choisie et comportement visé | Oracle attendu |
|---|---|---|
| `testGetNameWithNullInput` | `null` : entrée absente | `""` |
| `testGetNameWithEmptyInput` | `""` : entrée vide | `""` |
| `testGetNameWithUnixPath` | `/path/to/file.txt` : séparateur `/` | `file.txt` |
| `testGetNameWithWindowsPath` | `C:\path\to\file.txt` : séparateur Windows | `file.txt` |
| `testGetNameWithTrailingColon` | `file.txt:` : séparateur `:` final | `""` |
| `testGetNameWithMixedPathEndingInColon` | `C:/path/to/file.txt:` : séparateurs combinés, `:` final | `""` |
| `testGetNameWithDriveRelativePath` | `C:file.txt` : chemin relatif au lecteur Windows | `file.txt` |
| `testGetNamePreservesTrailingDot` | `file.txt.` : point terminal | `file.txt.` |
| `testGetNamePreservesInternalDoubleDot` | `file..txt` : deux points internes | `file..txt` |
| `testGetNamePreservesTrailingDoubleDot` | `file.txt..` : deux points terminaux | `file.txt..` |
| `testGetNamePreservesQuestionMark` | `file.txt?` : ponctuation dans le résultat de `getName` | `file.txt?` |

Les six cas dans `FilenameUtilsSanitizedGeneratedTest.java` portent sur `getSanitizedEmbeddedFileName`. Ils utilisent un objet `Metadata` construit dans le test. Pour les noms longs, `a + b × 100 + .txt` représente la chaîne Java `"a" + "b".repeat(100) + ".txt"`. La partie de nom retenue vaut 93 caractères (`a` et 92 `b`) et le suffixe ajouté vaut `...` suivi de `.txt` : le retour total est de 100 caractères.

| Test JUnit | Données choisies / but | Oracle attendu |
|---|---|---|
| `testEmptyResourceName` | `RESOURCE_NAME_KEY=""`, longueur 100 | `null` |
| `testBlankResourceName` | `RESOURCE_NAME_KEY="   "`, longueur 100 | `null` |
| `testLongNameWithShortDefaultExtension` | Nom long déjà terminé par `.txt`, extension par défaut `default` | Chaîne tronquée `a + b × 92 + ... + .txt` |
| `testLongNameWithLongDefaultExtension` | Même nom, extension par défaut `default.txt` | Même résultat tronqué |
| `testLongNameWithAnotherDefaultExtension` | Même nom, extension par défaut `longextension.txt` | Même résultat tronqué |
| `testLongNameWithExistingExtensionAndNullDefault` | Même nom, extension par défaut `null` | Même résultat tronqué |

**Analyse critique.** Les quatre derniers cas ont des paramètres d'extension par défaut différents, mais la méthode retient l'extension `.txt` déjà présente : ils sont partiellement **redondants**. Cette redondance explique qu'un ensemble de tests qui passe ne tue pas nécessairement davantage de mutants. À l'inverse, les valeurs `null`, vides, chemins Unix/Windows et fins de chaînes atypiques vérifient des comportements distincts.

## 4. Compilation, exécution et qualité des tests générés

Un oracle est le résultat attendu qu’un test compare au résultat du programme.

### Génération pour `getName`

ChatUniTest a produit 11 tests. Au premier essai où les tests ont été exécutés, 4 ont réussi et 7 ont échoué. Nous avons conservé les cas et corrigé les oracles des sept cas suivants après avoir vérifié le comportement de `FilenameUtils` :

| Entrée | Oracle initial incorrect | Oracle corrigé |
|---|---|---|
| `null` | `null` | `""` |
| `"file.txt:"` | `"file.txt"` | `""` |
| `"C:/path/to/file.txt:"` | `"file.txt"` | `""` |
| `"file.txt."` | `"file.txt"` | `"file.txt."` |
| `"file..txt"` | `""` | `"file..txt"` |
| `"file.txt.."` | `""` | `"file.txt.."` |
| `"file.txt?"` | `"file.txt"` | `"file.txt?"` |

Les autres cas couvrent notamment une chaîne vide et des chemins Unix et Windows. Les appels ont été corrigés pour utiliser la méthode statique; les imports inutiles de Mockito ont été retirés.

### Génération pour `getSanitizedEmbeddedFileName`

Les trois rounds de ChatUniTest ont échoué à compiler les propositions brutes. Un diagnostic montrait des chaînes passées à une méthode qui attend un objet `Metadata`. Nous avons retenu six cas parmi les propositions, créé des objets `Metadata` valides, puis corrigé les résultats attendus après vérification du code.

Pour le cas d’un nom long se terminant par `.txt`, avec une longueur maximale de 100, le résultat attendu contient un préfixe de **93 caractères** (`a` suivi de 92 `b`), `...`, puis `.txt`, soit exactement 100 caractères. Les autres cas retenus vérifient les métadonnées dont le nom est vide ou composé d’espaces, les extensions et les limites. Les propositions qui ne compilaient pas ou qui étaient redondantes n’ont pas été comptées comme des tests réussis.

### Comparaison qualitative

Les propositions de ChatUniTest ont fourni des cas utiles, mais leurs oracles n’étaient pas toujours conformes au comportement réel. Le cas `null` de `getName` et les noms avec points ou ponctuation montrent qu’une assertion plausible peut tout de même être fausse. Les tests de noms longs étaient aussi très similaires : plusieurs modifiaient l’extension par défaut sans ajouter un comportement réellement différent.

Les tests manuels ont ciblé des choix de propriétés de métadonnées et des frontières précises révélées par PIT. Ils complètent donc les exemples générés. Nous avons exécuté les tests après correction plutôt que de traiter le message `BUILD SUCCESS` de ChatUniTest comme une preuve suffisante : le plugin peut terminer avec succès même si des tests JUnit échouent.

## 5. Résultats PIT : quatre analyses reproduites et archivées

Nous avons exécuté **quatre analyses PIT 1.30.0 avec `pitest-junit5-plugin` 1.2.3**, sur la même classe de production `org.apache.tika.io.FilenameUtils` et le même ensemble de 115 mutations, en faisant varier uniquement les classes de tests exclues. Chaque exécution s'est terminée par `BUILD SUCCESS` le 12 octobre 2026 (heure locale). Les **quatre rapports `index.html` et `mutations.xml` sont désormais disponibles** dans le dossier `docs/pit-reports/` de l'archive de remise. Ces résultats sont calculés à partir des fichiers XML, pas d'estimations.

### 5.1 Scores comparés

| Suite de tests | Tués | Survivants | Sans couverture | Total | Score global | Couverture de lignes | Test strength |
|---|---:|---:|---:|---:|---:|---:|---:|
| Tests originaux | 73 | 20 | 22 | 115 | **63,48 %** | 153/175 (87 %) | 73/93 (78,49 %) |
| Originaux + 11 tests IA (`getName`) | 73 | 20 | 22 | 115 | **63,48 %** | 153/175 (87 %) | 73/93 (78,49 %) |
| Originaux + 17 tests IA | 74 | 19 | 22 | 115 | **64,35 %** | 153/175 (87 %) | 74/93 (79,57 %) |
| Originaux + 17 IA + 14 manuels | 94 | 6 | 15 | 115 | **81,74 %** | 160/175 (91 %) | 94/100 (94,00 %) |

Le **score de mutation global** est calculé comme `mutants KILLED / tous les mutants générés`. Ainsi, le score final vaut `94 / 115 × 100 = 81,74 %`. Les mutants `NO_COVERAGE` restent dans le dénominateur. La *test strength* est une métrique distincte : `KILLED / (115 − NO_COVERAGE)`.

**Progression :** 73 → 73 → 74 → 94 mutants tués, soit **+21 mutants tués** et **+18,26 points** par rapport à la suite d'origine. Les 11 tests IA de `getName` n'ont pas modifié le score ; les six tests IA supplémentaires ont tué un mutant de plus ; les 14 tests manuels en ont tué 20 de plus.

### 5.2 Rapports à consulter

| Phase | Rapport HTML | Mutations XML |
|---|---|---|
| 01 — Tests existants | [HTML](docs/pit-reports/01-originaux/index.html) | [XML](docs/pit-reports/01-originaux/mutations.xml) |
| 02 — + 11 IA | [HTML](docs/pit-reports/02-ia11/index.html) | [XML](docs/pit-reports/02-ia11/mutations.xml) |
| 03 — + 17 IA | [HTML](docs/pit-reports/03-ia17/index.html) | [XML](docs/pit-reports/03-ia17/mutations.xml) |
| 04 — + 14 manuels | [HTML](docs/pit-reports/04-final-recalcule/index.html) | [XML](docs/pit-reports/04-final-recalcule/mutations.xml) |


Les pages HTML peuvent être ouvertes localement après clonage du dépôt (GitHub ne garantit pas leur rendu directement dans le navigateur). Chaque rapport dispose aussi de `style.css`, de la page de détails `org.apache.tika.io/FilenameUtils.java.html` et de l'index du paquetage. Le XML permet les comparaisons exactes des mutants et fournit le champ `killingTest`.

### 5.3 Protocole de comparaison reproductible

Commande générale exécutée depuis la racine du projet :

```powershell
.\mvnw.cmd -B -ntp -pl tika-core test-compile org.pitest:pitest-maven:mutationCoverage
```

Nous avons défini `targetClasses = org.apache.tika.io.FilenameUtils` dans `tika-core/pom.xml`, puis exclu successivement les classes des tests supplémentaires avec `-DexcludedTestClasses=...` :

| Phase | Classes de tests supplémentaires exclues |
|---|---|
| 01 — originaux | `FilenameUtilsGeneratedTest`, `FilenameUtilsSanitizedGeneratedTest`, `FilenameUtilsMutationTest` |
| 02 — IA11 | `FilenameUtilsSanitizedGeneratedTest`, `FilenameUtilsMutationTest` |
| 03 — IA17 | `FilenameUtilsMutationTest` |
| 04 — final | Aucune |

Les autres tests d'origine susceptibles de couvrir `FilenameUtils` sont conservés pendant les quatre analyses ; seul le jeu de classes ajoutées varie. La sortie PIT indique ainsi successivement **379, 380, 381 et 382 classes de tests examinées**. Les rapports sont sauvegardés **après chaque exécution**, car le répertoire `tika-core/target/pit-reports` peut être réécrit. Les paramètres de production, les mutateurs et la version du moteur PIT ne changent pas entre les étapes.

### 5.4 Vérification de l'identité des mutants

Nous avons comparé chaque entrée XML avec la clé complète `(mutatedClass, mutatedMethod, methodDescription, lineNumber, mutator, index, block)`. Les quatre fichiers comportent **les mêmes 115 clés**, ce qui permet d'attribuer chaque changement de statut à l'ajout des tests, et non à un changement de population de mutants. Les fichiers XML indiquent `partial="true"` dans leur en-tête ; les 115 entrées et tous les statuts annoncés ont néanmoins été vérifiés dans chacun des rapports archivés.

## 6. Documentation des mutants : comparaison ligne par ligne des quatre XML

**Méthode.** Un mutant est marqué `KILLED` lorsqu'une assertion échoue sur le programme muté. `SURVIVED` signifie qu'il a été exercé sans être détecté ; `NO_COVERAGE` qu'aucun test pertinent ne l'a exercé. La comparaison utilise les identifiants PIT, pas seulement la différence entre scores. La colonne **test tueur** reproduit exactement le nom de méthode mentionné par le champ `killingTest` de la phase qui tue pour la première fois le mutant. Cela identifie un test responsable constaté par PIT, sans signifier qu'il est l'unique test capable de tuer ce mutant.

### 6.1 Effet des 11 premiers tests IA

Entre les phases `01-originaux` et `02-ia11`, **aucun des 115 mutants ne change de statut**. Les 11 tests ajoutés vérifient surtout les entrées vides, les délimiteurs Unix/Windows/`:` et la ponctuation de `getName`, mais les mutants que PIT crée dans cette méthode sont déjà tués par la suite existante (`FilenameUtilsTest`). Cela souligne que des tests corrects et spécifiques peuvent être redondants pour la mutation.

### 6.2 Mutant réellement détecté par les six autres tests générés

Entre `02-ia11` et `03-ia17`, nous observons **exactement une transition `SURVIVED → KILLED`**, et son `killingTest` appartient bien à la classe générée par ChatUniTest puis corrigée :

| Méthode et ligne | Mutateur PIT | Transition | Test tueur (`killingTest`) | Pourquoi le test détecte la mutation |
|---|---|---|---|---|
| `getEmbeddedName:365` | condition inversée (`index=11`, `block=2`) | `SURVIVED → KILLED` | `testLongNameWithLongDefaultExtension` | Le nom `RESOURCE_NAME_KEY` est non vide ; l'inversion de sa garde le fait ignorer et empêche de conserver exactement le nom long attendu. |

**Explication technique.** Dans `getEmbeddedName`, la méthode commence par lire `TikaCoreProperties.RESOURCE_NAME_KEY` et retourne sa valeur quand elle n'est pas vide. Le mutateur `NegateConditionalsMutator` inverse cette garde à la **ligne 365**. Le test `testLongNameWithLongDefaultExtension` définit un nom long valide et vérifie exactement le résultat attendu après troncature (`"a" + "b".repeat(92) + "..." + ".txt"`, avec `maxLength=100`). Si la sélection du nom initial est perturbée, le résultat diffère de cet oracle. **Ce mutant précis est le gain attribuable aux tests IA** dans nos quatre exécutions.

Les cinq autres tests de cette classe restent pertinents pour leur comportement, mais aucune autre entrée PIT ne passe à `KILLED` lors de cette étape.

### 6.3 Les 20 mutants nouvellement tués par les tests manuels

Entre `03-ia17` et `04-final-recalcule`, les **20 transitions** se répartissent en **13 `SURVIVED → KILLED`** et **7 `NO_COVERAGE → KILLED`**. Chacune est attribuée ci-dessous à un test effectivement enregistré par PIT :

| Méthode et ligne | Mutateur PIT | Transition | Test tueur (`killingTest`) | Pourquoi le test détecte la mutation |
|---|---|---|---|---|
| `getEmbeddedName:369` | condition inversée (`index=27`, `block=6`) | `SURVIVED → KILLED` | `testEmbeddedNameFromRelationshipId` | En l'absence de `INTERNAL_PATH`, le repli doit continuer ; la condition inversée interrompt la recherche avant la valeur attendue. |
| `getEmbeddedName:370` | retour remplacé par chaîne vide (`index=31`, `block=7`) | `NO_COVERAGE → KILLED` | `testEmbeddedNameFromInternalPath` | Avec `INTERNAL_PATH` seul, le retour exact doit être `internal.txt` ; une chaîne vide enfreint `assertEquals`. |
| `getEmbeddedName:373` | condition inversée (`index=43`, `block=10`) | `SURVIVED → KILLED` | `testEmbeddedNameFromRelationshipId` | La propriété `EMBEDDED_RELATIONSHIP_ID` doit être sélectionnée lorsqu'elle est renseignée ; la branche inverse conduit à une valeur incorrecte. |
| `getEmbeddedName:374` | retour remplacé par chaîne vide (`index=47`, `block=11`) | `NO_COVERAGE → KILLED` | `testEmbeddedNameFromRelationshipId` | Le retour du nom issu de `EMBEDDED_RELATIONSHIP_ID` ne doit pas devenir `""` (attendu `relationship.txt`). |
| `getEmbeddedName:377` | condition inversée (`index=59`, `block=14`) | `SURVIVED → KILLED` | `testEmbeddedNameFromOriginalResourceName` | La propriété de chemin embarqué est absente dans le cas du dernier repli ; la condition inversée empêche d'atteindre le nom original attendu. |
| `getEmbeddedName:378` | retour remplacé par chaîne vide (`index=63`, `block=15`) | `NO_COVERAGE → KILLED` | `testEmbeddedNameFromEmbeddedResourcePath` | La propriété `EMBEDDED_RESOURCE_PATH` est seule renseignée ; substituer `""` au nom `embedded.txt` fait échouer l'oracle. |
| `getEmbeddedName:380` | retour remplacé par chaîne vide (`index=70`, `block=17`) | `SURVIVED → KILLED` | `testEmbeddedNameFromOriginalResourceName` | Le dernier repli doit renvoyer `original.txt` ; remplacer ce retour par `""` est détecté. |
| `getEmbeddedPath:344` | condition inversée (`index=11`, `block=2`) | `SURVIVED → KILLED` | `testEmbeddedPathFromEmbeddedResourcePath` | `EMBEDDED_RESOURCE_PATH` contient `resource/file.txt` ; inverser sa garde empêche de retourner ce chemin. |
| `getEmbeddedPath:348` | condition inversée (`index=27`, `block=6`) | `SURVIVED → KILLED` | `testEmbeddedPathFromResourceName` | `INTERNAL_PATH` est absent dans le scénario qui utilise `RESOURCE_NAME_KEY` ; la branche inversée empêche le repli attendu. |
| `getEmbeddedPath:349` | retour remplacé par chaîne vide (`index=31`, `block=7`) | `NO_COVERAGE → KILLED` | `testEmbeddedPathFromInternalPath` | Avec `INTERNAL_PATH` seul, `internal/file.txt` doit être renvoyé ; `""` est incorrect. |
| `getEmbeddedPath:352` | condition inversée (`index=43`, `block=10`) | `SURVIVED → KILLED` | `testEmbeddedPathFromResourceName` | `RESOURCE_NAME_KEY` est présent ; la mutation contourne son retour et perd `resourceName/file.txt`. |
| `getEmbeddedPath:353` | retour remplacé par chaîne vide (`index=47`, `block=11`) | `NO_COVERAGE → KILLED` | `testEmbeddedPathFromResourceName` | Le retour de `RESOURCE_NAME_KEY` est remplacé par `""`, au lieu de `resourceName/file.txt`. |
| `getEmbeddedPath:356` | condition inversée (`index=59`, `block=14`) | `SURVIVED → KILLED` | `testEmbeddedPathFromOriginalResourceName` | `EMBEDDED_RELATIONSHIP_ID` est absent dans le scénario du repli final ; la branche inversée perturbe l'accès à `ORIGINAL_RESOURCE_NAME`. |
| `getEmbeddedPath:357` | retour remplacé par chaîne vide (`index=63`, `block=15`) | `NO_COVERAGE → KILLED` | `testEmbeddedPathFromRelationshipId` | `EMBEDDED_RELATIONSHIP_ID` contient `relationship/file.txt` ; renvoyer `""` viole l'assertion. |
| `getEmbeddedPath:359` | retour remplacé par chaîne vide (`index=70`, `block=17`) | `SURVIVED → KILLED` | `testEmbeddedPathFromOriginalResourceName` | `ORIGINAL_RESOURCE_NAME` est le dernier repli ; remplacer `original/file.txt` par `""` est détecté. |
| `getSanitizedEmbeddedFileName:189` | frontière modifiée (`index=184`, `block=43`) | `SURVIVED → KILLED` | `testEmbeddedFileNameExactlyAtMaxLength` | La partie nom `abcdefghij` mesure exactement `maxLength=10`. L'original ne tronque pas, tandis que la mutation `>` → `>=` tronque à tort. |
| `getSanitizedEmbeddedFilePath:264` | frontière modifiée (`index=261`, `block=58`) | `SURVIVED → KILLED` | `testEmbeddedPathExactlyAtMaxLength` | `a/b.txt` mesure exactement 7 caractères pour `maxLength=7`. La mutation du seuil ferait perdre le préfixe `a/`. |
| `getSanitizedEmbeddedFilePath:265` | frontière modifiée (`index=267`, `block=60`) | `SURVIVED → KILLED` | `testEmbeddedPathNamePartExactlyAtMaxLength` | Dans `dir/abcdefghij.txt`, le chemin est trop long mais la partie nom mesure exactement 10 ; `>` → `>=` provoquerait une troncature au lieu de garder `abcdefghij.txt`. |
| `getSanitizedEmbeddedFilePath:268` | retour remplacé par chaîne vide (`index=288`, `block=64`) | `NO_COVERAGE → KILLED` | `testEmbeddedPathNamePartExactlyAtMaxLength` | Quand le répertoire est abandonné pour respecter la limite, la méthode doit retourner `abcdefghij.txt` et non une chaîne vide. |
| `getSuffixFromPath:134` | frontière modifiée (`index=22`, `block=4`) | `SURVIVED → KILLED` | `testSuffixExactlyFiveCharactersIsRejected` | Avec `file.abcde`, les cinq caractères après le point ne satisfont pas `n.length()-i < 6` (écart 6) ; `<=` accepterait à tort `.abcde`. |

**Synthèse par méthode :** `getEmbeddedPath` (**8 mutants tués de plus**), `getEmbeddedName` (**7**), `getSanitizedEmbeddedFilePath` (**3**), `getSanitizedEmbeddedFileName` (**1**) et `getSuffixFromPath` (**1**). **Par mutateur :** `EmptyObjectReturnValsMutator` (**9**), `NegateConditionalsMutator` (**7**), `ConditionalsBoundaryMutator` (**4**). Il ne s'agit pas d'une extrapolation : ces 20 entrées précises sont identifiées dans les deux XML.

Les tests de repli de métadonnées n'initialisent volontairement qu'une seule propriété à la fois. Cette isolation fait échouer les mutants qui inversent la garde ou remplacent une valeur de retour par `""`. Les cas frontières (`file.abcde`, nom de longueur maximale et chemin de longueur maximale) distinguent précisément `>` de `>=` ou `<` de `<=`.

### 6.4 Mutants toujours vivants : limites observées

Les **six mutants survivants** du rapport final sont :

| Méthode : ligne | Opération PIT | Motif de survie ou comportement encore à vérifier |
|---|---|---|
| `getPrefixLength:320` | frontière modifiée, `index=9` | Branches de préfixe de chemin qui ne sont pas distinguées par les assertions actuelles. |
| `getPrefixLength:323` | condition inversée, `index=20` | Branches de préfixe de chemin qui ne sont pas distinguées par les assertions actuelles. |
| `getSanitizedEmbeddedFileName:156` | frontière modifiée, `index=52` | Branches de préfixe de chemin qui ne sont pas distinguées par les assertions actuelles. |
| `getSanitizedEmbeddedFileName:156` | condition inversée, `index=52` | Branches de préfixe de chemin qui ne sont pas distinguées par les assertions actuelles. |
| `getSanitizedEmbeddedFilePath:215` | frontière modifiée, `index=31` | Branches de préfixe de chemin qui ne sont pas distinguées par les assertions actuelles. |
| `resolveWithin:305` | condition inversée, `index=38` | Vérification des chemins présents / liens symboliques encore insuffisamment discriminée. |

Les **15 `NO_COVERAGE`** restants incluent des retours défensifs et des branches de normalisation/préfixe (`getSanitizedEmbeddedFileName`, `getSanitizedEmbeddedFilePath`, `getPrefixLength`), des cas de `resolveWithin`, ainsi que des chemins de retour dans `calculateExtension` et `lookupExtension`. Ces mutants ne sont **pas** déclarés tués ; aucune équivalence n'est affirmée sans preuve. Leur liste exacte et leurs index se trouvent dans [`04-final-recalcule/mutations.xml`](docs/pit-reports/04-final-recalcule/mutations.xml).

**Bilan :** `94 KILLED + 6 SURVIVED + 15 NO_COVERAGE = 115`. Les tests générés ne détectent donc pas tous les mutants ; l'ajout manuel de tests a eu un effet mesuré important, sans atteindre 100 %.

## 7. Tests écrits manuellement : nom, intention, données, motivation et oracle

Les **14 tests manuels** figurent dans [`FilenameUtilsMutationTest.java`](tika-core/src/test/java/org/apache/tika/io/FilenameUtilsMutationTest.java). Chaque test fixe une **entrée contrôlée**, puis vérifie une **valeur exacte**. Les valeurs `resource.txt`, `internal.txt`, etc. sont volontairement différentes : elles rendent visible une erreur de sélection de propriété. Les valeurs de frontières servent à détecter les changements de conditions que les exemples ordinaires ne distinguent pas.

| Test | Comportement et intention | Donnée choisie et motivation | Oracle : comportement attendu et justification |
|---|---|---|---|
| `testEmbeddedPathFromEmbeddedResourcePath` | Tester la priorité du chemin embarqué. | Seulement `EMBEDDED_RESOURCE_PATH="resource/file.txt"` pour isoler cette clé. | `"resource/file.txt"` : le chemin existe et ne contient aucun caractère à nettoyer. |
| `testEmbeddedPathFromInternalPath` | Tester le repli chemin interne. | Seulement `INTERNAL_PATH="internal/file.txt"`, sans chemin embarqué. | `"internal/file.txt"` : `getEmbeddedPath` utilise la prochaine clé disponible. |
| `testEmbeddedPathFromResourceName` | Tester le repli vers le nom de ressource. | Seulement `RESOURCE_NAME_KEY="resourceName/file.txt"`. | `"resourceName/file.txt"` : la valeur est conservée comme chemin relatif normalisé. |
| `testEmbeddedPathFromRelationshipId` | Tester le repli vers l'identifiant de relation. | Seulement `EMBEDDED_RELATIONSHIP_ID="relationship/file.txt"`. | `"relationship/file.txt"` : absence de clés prioritaires, utilisation de celle-ci. |
| `testEmbeddedPathFromOriginalResourceName` | Tester le dernier repli du chemin. | Seulement `ORIGINAL_RESOURCE_NAME="original/file.txt"`. | `"original/file.txt"` : dernière valeur disponible, conservée sans modification. |
| `testEmbeddedNameFromResourceName` | Tester la clé prioritaire du nom. | Seulement `RESOURCE_NAME_KEY="resource.txt"`. | `"resource.txt"` : le nom contient une extension déjà présente et valide. |
| `testEmbeddedNameFromInternalPath` | Tester le repli interne du nom. | Seulement `INTERNAL_PATH="internal.txt"`. | `"internal.txt"` : les propriétés plus prioritaires sont absentes. |
| `testEmbeddedNameFromRelationshipId` | Tester le repli identifiant du nom. | Seulement `EMBEDDED_RELATIONSHIP_ID="relationship.txt"`. | `"relationship.txt"` : `getEmbeddedName` récupère cette clé. |
| `testEmbeddedNameFromEmbeddedResourcePath` | Tester le repli chemin embarqué du nom. | Seulement `EMBEDDED_RESOURCE_PATH="embedded.txt"`. | `"embedded.txt"` : la valeur est reconnue comme nom sans séparateur à extraire. |
| `testEmbeddedNameFromOriginalResourceName` | Tester le dernier repli du nom. | Seulement `ORIGINAL_RESOURCE_NAME="original.txt"`. | `"original.txt"` : dernière clé admissible quand les autres sont absentes. |
| `testSuffixExactlyFiveCharactersIsRejected` | Vérifier la frontière du suffixe. | `file.abcde`, **5 caractères après le point**; distingue `<` de `<=` dans le contrôle de longueur. | `""` : dans le code courant, la condition `n.length() - i < 6` rejette ce suffixe. |
| `testEmbeddedFileNameExactlyAtMaxLength` | Vérifier la frontière de la **partie nom**. | `abcdefghij.txt`, `maxLength=10`; la partie `abcdefghij` contient 10 caractères. | `"abcdefghij.txt"` : le code tronque quand la partie nom est **strictement** plus longue que 10, pas lorsqu'elle vaut 10. |
| `testEmbeddedPathExactlyAtMaxLength` | Vérifier la frontière de la longueur totale d'un chemin. | `a/b.txt`, longueur totale 7, `maxLength=7`. | `"a/b.txt"` : le chemin n'excède pas la limite. |
| `testEmbeddedPathNamePartExactlyAtMaxLength` | Vérifier un chemin trop long, mais une partie nom au seuil. | `dir/abcdefghij.txt`, `maxLength=10` : chemin total long, partie nom exactement 10. | `"abcdefghij.txt"` : le traitement perd le répertoire, mais garde le nom et son extension. |

Pour les dix premiers tests, **une seule propriété de `Metadata` est définie** : c'est essentiel pour prouver le comportement de repli. Les oracles sont des constantes vérifiées par `assertEquals` — pas des valeurs produites par une seconde exécution de la méthode testée. Pour les quatre tests de limites, le résultat attendu est justifié par les conditions du code courant (méthodes `getSuffixFromPath`, `getSanitizedEmbeddedFileName` et `getSanitizedEmbeddedFilePath`).

## 8. Exécution locale et GitHub Actions

La commande locale ci-dessous exécute les trois classes ajoutées :

```powershell
mvn -pl tika-core "-Dtest=FilenameUtilsGeneratedTest,FilenameUtilsSanitizedGeneratedTest,FilenameUtilsMutationTest" test
```

Résultat observé localement : **31 tests exécutés, 0 échec, 0 erreur, 0 test ignoré**. Une exécution locale plus large de `tika-core` avait également donné `BUILD SUCCESS`, avec 780 tests exécutés, 0 échec, 0 erreur et 3 tests ignorés; ce second chiffre provient des journaux locaux, et non du workflow ciblé présenté ci-dessous.

Le workflow propre à la tâche est [`.github/workflows/tache2-tests.yml`](.github/workflows/tache2-tests.yml). Il utilise `windows-latest`, installe Java 17 et prépare le module `tika-annotation-processor` avant de lancer les trois classes de test dans `tika-core`.

**Preuve distante vérifiée :** l'[exécution GitHub Actions n° 4 du 10 octobre 2026](https://github.com/rayenrizk/tika/actions/runs/38023804518), sur le commit [`801ea2c`](https://github.com/rayenrizk/tika/commit/801ea2c4a81a84dab69f1a2ce055434193bae97a), a obtenu la conclusion **`success`**. Ses journaux indiquent explicitement :

```text
FilenameUtilsGeneratedTest           11 tests, 0 échec, 0 erreur
FilenameUtilsSanitizedGeneratedTest   6 tests, 0 échec, 0 erreur
FilenameUtilsMutationTest            14 tests, 0 échec, 0 erreur
Total                                31 tests, 0 échec, 0 erreur, 0 ignoré
BUILD SUCCESS
```

Les workflows généraux hérités du projet Apache Tika ne sont pas l'équivalent du workflow de la tâche; certains affichent des échecs. Les tests JUnit de la tâche sont donc validés; le workflow ciblé **ne relance pas PIT** et ne permet pas à lui seul de confirmer les scores de mutation. L’exécution des 31 nouveaux tests est vérifiée par le workflow **Tache 2 - Tests** pour le commit ci-dessus. Pour une révision ultérieure, vérifier à nouveau le workflow sur le nouveau commit.

Les avertissements Maven sur la version `${revision}` du projet et l’avertissement JaCoCo sur les données d’exécution ne sont pas des échecs JUnit. Les tests ciblés ont terminé avec `BUILD SUCCESS`.

## 9. Bilan et limites

Le travail a porté sur **une classe de production** qui possédait déjà des tests mais présentait des mutants vivants. Après la génération et la correction de **17 tests issus de ChatUniTest**, nous avons ajouté **14 tests manuels**. La comparaison de **quatre véritables rapports PIT XML/HTML** confirme le passage de **73 à 94 mutants tués sur 115** ; le score progresse de **63,48 % à 81,74 %**, et les tests manuels expliquent à eux seuls **20 mutations nouvellement éliminées**. Le workflow GitHub Actions dédié exécute avec succès les **31 nouveaux tests**.

La **traçabilité des mutations ajoutées est désormais complète pour ces quatre exécutions** : identité PIT, évolution de statut et `killingTest` sont disponibles dans les XML et expliqués au § 6. Les six survivants et quinze mutants non couverts restent des limites techniques. Les données brutes non retenues de ChatUniTest ne sont pas toutes versionnées ; elles ne sont pas présentées comme des tests exécutés. La reproduction des générations IA dépend d'Ollama installé localement, mais la correspondance des deux noms du modèle a été vérifiée sur le poste utilisé.

**Limite de publication :** le présent document et ses pièces PIT sont fournis prêts à intégrer au dépôt, mais ils doivent encore être poussés sur GitHub pour devenir des liens de preuves consultables par l'enseignant.

Les objectifs pédagogiques atteints et démontrables sont les suivants : (a) tests produits avec un outil d'IA puis **critiqués**, (b) oracles incorrects **corrigés**, (c) mutations utilisées comme mesure de la pertinence de la suite et de sa progression, (d) tests écrits **manuellement** pour explorer les chemins et frontières manquants, (e) **exécution automatique** reproductible des 31 cas sur GitHub Actions. Une note ou une conformité intégrales dépend de l'évaluation du professeur et, pour la documentation des mutants, de la publication des rapports PIT comparables.

### Déclaration d'utilisation des outils d'IA

**ChatUniTest avec Ollama** a été employé pour générer des propositions de tests dans le cadre du sujet. Les propositions ont été critiquées, leurs oracles et leur usage des objets `Metadata` ont été corrigés, et tous les tests retenus ont été exécutés. **ChatGPT (OpenAI)** a également été utilisé comme aide à la compréhension des consignes, à la rédaction du compte rendu et à la comparaison structurée des fichiers XML PIT. Les nombres, les changements de statut et les noms des tests tueurs ont été vérifiés dans les quatre rapports PIT ; ils ne proviennent pas d'une estimation de l'IA. Le binôme reste responsable des expériences, du code et de la remise.

## 10. Sources et outils

- [Énoncé officiel de la tâche 2](https://github.com/umontreal-diro/IFT3913/blob/2026/tache2/readme.md)
- [ChatUniTest](https://github.com/ZJU-ACES-ISE/ChatUniTest)
- [PIT](https://pitest.org/)
- Tests exécutés avec Java 17, Maven et JUnit 5.

Les tests générés par l’IA ont été relus et corrigés avant leur intégration. ChatUniTest a servi à proposer des cas; les assertions finales et leur validation restent la responsabilité du binôme.


## 11. Renseignements pour la remise au cours

Selon [l'énoncé officiel de la tâche 2](https://github.com/umontreal-diro/IFT3913/blob/2026/tache2/readme.md), une **nouvelle pull request** doit modifier le fichier `tache2/RIZK_OUAMMOU/readme.md` dans le dépôt du cours et y ajouter **les deux liens suivants** :

- **Lien vers le dépôt GitHub de la tâche :** https://github.com/rayenrizk/tika
- **Lien vers le README qui documente la tâche :** https://github.com/rayenrizk/tika/blob/main/README.md

Le README du dépôt du cours doit garder le [format officiel](https://github.com/umontreal-diro/IFT3913/blob/2026/.github/PULL_REQUEST_TEMPLATE/tache2-readme.md) avec les deux noms et identifiants GitHub. L'ajout des liens et l'ouverture de la pull request sont **des étapes de remise distinctes** de l'écriture de ce document. Le fichier `README.md` à la racine du dépôt `rayenrizk/tika` affiche encore la présentation générale d’Apache Tika à la date de cette rédaction. Il faut y publier **ce `README.md`** pour que le lien de remise pointe réellement vers la documentation de la tâche. Cette publication n'est pas effectuée par la création du fichier local téléchargé.
