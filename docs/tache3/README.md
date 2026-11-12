# IFT3913 — Tâche 3 : automatisation des tests de sécurité

**Binôme :** Rayen Rizk (`rayenrizk`) et Mohamed Badr Ouammou (`gy23578`)  
**Dépôt de travail :** https://github.com/rayenrizk/tika  
**Branche :** [tache3-semgrep](https://github.com/rayenrizk/tika/tree/tache3-semgrep)  
**Pull request de démonstration :** https://github.com/rayenrizk/tika/pull/1  
**Projet étudié :** Apache Tika, module `tika-core`.

> **Important :** les sept classes `Semgrep*Demo` sont des démonstrations de vulnérabilités introduites volontairement pour un exercice universitaire. Elles ne doivent pas être fusionnées dans une branche destinée à la production. Elles sont indépendantes du chemin normal d'exécution de Tika : leur détection statique ne démontre pas qu'une exploitation est possible dans l'application déployée.

## 1. Objectif et périmètre

Le travail consiste à ajouter à l'intégration continue un contrôle Semgrep qui bloque les **nouvelles** alertes, à comparer les rapports avant/après, à introduire sept types de vulnérabilités détectables et à ajouter un Rickroll après un échec de tests.

Le module `tika-core` a été retenu parce qu'il fait partie des modules autorisés par le cours et qu'il peut être vérifié indépendamment grâce à Maven. Les exemples sont placés dans `tika-core/src/main/java/org/apache/tika/parser/`. Les fichiers ont été créés spécialement pour l'expérience ; ils ne remplacent pas une logique de traitement de Tika. Ce choix permet d'isoler les défauts et de conserver la compilation, mais constitue une **limite** de représentativité par rapport à des vulnérabilités introduites dans des fonctions réellement utilisées.

## 2. Intégration de Semgrep dans GitHub Actions

**Fichier :** [`.github/workflows/main-jdk17-build.yml`](../../.github/workflows/main-jdk17-build.yml)

Un job `semgrep-security` :
1. récupère l'historique Git avec `actions/checkout@v6` et `fetch-depth: 0` ;
2. installe Python 3.12 et Semgrep 1.185.0 ;
3. calcule le commit de référence : `git merge-base HEAD "$PR_BASE_SHA"` pour une pull request, ou `HEAD^` pour un push ;
4. exécute `semgrep scan --config p/security-audit --baseline-commit "$BASE_SHA" --error --json --output semgrep-nouveaux.json tika-core/src/main/java` ;
5. sauvegarde le JSON sous forme d'artifact, même si l'analyse échoue.

Le job `build` dépend de `semgrep-security` (`needs: semgrep-security`) et `e2e-tests` dépend de `build`. L'option `--error` rend l'étape Semgrep bloquante lorsque de nouvelles alertes sont relevées. L'utilisation de `--baseline-commit` vise à ne pas confondre les problèmes historiques avec ceux ajoutés par cette branche.

**Limites :** le contrôle cible seulement `tika-core/src/main/java`. Pour un push, `HEAD^` compare avec le parent immédiat plutôt qu'avec une branche de référence permanente ; le comportement est plus pertinent sur une pull request. Les règles distantes `p/security-audit` peuvent évoluer dans le temps.

### Validation de la GitHub Action

- **Contrôle sans nouvelle vulnérabilité :** [exécution GitHub Actions réussie du job Semgrep](https://github.com/rayenrizk/tika/actions/runs/43259509218), avant l'ajout des classes de démonstration. Résultat : zéro nouvelle alerte bloquante.
- **Contrôle avec une nouvelle vulnérabilité :** [exécution GitHub Actions après l'ajout de `SemgrepSocketDemo.java`](https://github.com/rayenrizk/tika/actions/runs/43261720974). Résultat : une nouvelle alerte bloquante, étape `Detecter les nouvelles vulnerabilites` terminée avec le code de sortie 1 ; les jobs de build dépendants ont été ignorés.
- **Contrôles locaux supplémentaires :** exécution de la même configuration et du même commit de référence après chaque ajout, puis vérification du nombre de règles distinctes. Après les sept ajouts, le scan ciblé a fourni sept alertes bloquantes appartenant à sept règles distinctes (journal local du 12 novembre 2026). Le module a aussi passé `mvnw.cmd -pl tika-core -am compile -DskipTests` avec zéro violation Checkstyle et `BUILD SUCCESS`.

Le succès de `compile -DskipTests` **ne signifie pas** que tous les tests Maven ou les workflows indépendants ont réussi.

## 3. Sept vulnérabilités ajoutées

Tous les exemples sont situés dans le même module autorisé, `tika-core`. La justification commune du répertoire est la possibilité de compiler et d'analyser les classes Java de manière reproductible. Pour chaque cas, l'emplacement reste un **choix de démonstration** et non la preuve que Tika exécute réellement le code vulnérable.

| N° | Fichier ajouté | Construction vulnérable | Règle Semgrep détectée | Risque et justification |
|---|---|---|---|---|
| 1 | `SemgrepSocketDemo.java` | `new Socket(host, port)` | `unencrypted-socket` | Une connexion réseau non chiffrée peut exposer les données transmises à l'interception. |
| 2 | `SemgrepWeakHashDemo.java` | `MessageDigest.getInstance("SHA-1")` | `use-of-sha1` | SHA-1 est insuffisant pour les usages qui exigent la résistance aux collisions, notamment les signatures numériques. |
| 3 | `SemgrepDeserializationDemo.java` | `new ObjectInputStream(input)` puis `readObject()` | `object-deserialization` | Désérialiser un contenu non fiable sans restrictions peut mener à un comportement dangereux selon les classes disponibles. |
| 4 | `SemgrepCbcDemo.java` | `Cipher.getInstance("AES/CBC/PKCS5Padding")` | `cbc-padding-oracle` | Le mode CBC avec bourrage sans authentification peut contribuer à une attaque de type padding oracle si une application divulgue les erreurs de déchiffrement. |
| 5 | `SemgrepWeakRandomDemo.java` | `java.lang.Math.random()` | `weak-random` | Un générateur non cryptographique ne convient pas à la création de secrets ou de jetons sensibles. |
| 6 | `SemgrepNullCipherDemo.java` | `new NullCipher()` | `no-null-cipher` | `NullCipher` ne chiffre pas les données : il ne fournit aucune confidentialité. |
| 7 | `SemgrepStaticIvDemo.java` | Tableau `byte[] iv` constant puis `new IvParameterSpec(iv)` | `no-static-initialization-vector` | Un IV fixe réutilisé dans un mode tel que CBC compromet certaines garanties de confidentialité. |

La **preuve de détection** de chaque cas se trouve dans le rapport JSON Semgrep local `semgrep-test7.json`, qui donne le chemin, la règle `check_id` et la ligne pour chacun. Les détections sont statiques : elles ne constituent pas des essais d'exploitation.

### Reproduire les sept détections depuis la branche

Depuis la racine du dépôt :

```powershell
semgrep scan --config p/security-audit --baseline-commit 9ba095be8eb0d21274a265e01a60e2c81caff217 --error --json --output semgrep-test7.json tika-core/src/main/java
```

Résultat obtenu localement : **7 findings, 7 blocking**, avec **7 identifiants de règles différents**.

Pour vérifier les règles :

```powershell
$r = Get-Content semgrep-test7.json -Raw | ConvertFrom-Json
$r.results | Select-Object check_id, path, @{Name="Ligne";Expression={$_.start.line}}
```

## 4. Comparaison avant / après

**Analyse initiale :** fichier [`semgrep-avant.json`](../../semgrep-avant.json), obtenu avant la création des sept classes.

Deux alertes initiales étaient présentes dans `tika-core` :

1. `unencrypted-socket` dans `NetworkParser.java` ;
2. `xmlinputfactory-possible-xxe` dans `XMLReaderUtils.java`.

Ces alertes sont **préexistantes** et ne doivent pas être présentées comme introduites par le travail.

**Après cinq vulnérabilités :** le scan complet réalisé le 12 novembre 2026 signalait **7 alertes : 2 anciennes et 5 nouvelles**.

**Après sept vulnérabilités :** le scan différentiel a confirmé **7 nouvelles alertes distinctes**. Le **scan complet final a aussi été exécuté et vérifié sur GitHub Actions le 12 novembre 2026 : 9 alertes au total, dont 2 préexistantes et 7 nouvelles**. Le job a réussi et a produit l'artifact `comparatif-semgrep-avant-apres`. [Exécution de preuve](https://github.com/rayenrizk/tika/actions/runs/43273761102).

Reproduire la comparaison complète :

```powershell
semgrep scan --config p/security-audit --json --output semgrep-apres-final.json tika-core/src/main/java
$avant = Get-Content semgrep-avant.json -Raw | ConvertFrom-Json
$apres = Get-Content semgrep-apres-final.json -Raw | ConvertFrom-Json
"Avant: $($avant.results.Count)"
"Apres: $($apres.results.Count)"
```

Vérifier également les chemins des alertes pour distinguer les sept classes ajoutées des deux fichiers anciens.

### Vérification automatisée du scan complet

Un second workflow, [`.github/workflows/tache3-rapport-semgrep.yml`](../../.github/workflows/tache3-rapport-semgrep.yml), a été ajouté afin de produire une **preuve reproductible** de la comparaison complète. Cette action lance le même ensemble de règles `p/security-audit` sur le répertoire Java de Tika, écrit `docs/tache3/semgrep-apres-final.json`, puis compare les paires (règle, chemin) avec le fichier initial `semgrep-avant.json`. Elle archive les deux fichiers JSON comme artifact `comparatif-semgrep-avant-apres` et affiche les comptes dans le résumé du job.

**Exécution de contrôle validée :** [GitHub Actions — rapport Semgrep complet](https://github.com/rayenrizk/tika/actions/runs/43273761102) : **succès**, 331 fichiers Java analysés, 9 résultats au scan complet, 2 avant les changements, 7 nouveaux. Les deux JSON sont sauvegardés dans l'artifact `comparatif-semgrep-avant-apres`. Cet ajout améliore la traçabilité sans modifier le comportement du job bloquant `semgrep-security`.

## 5. Rickroll en cas d'échec de tests

**Action réutilisable :** [`.github/actions/rickroll/action.yml`](../../.github/actions/rickroll/action.yml)  
**Workflow utilisateur :** [`.github/workflows/tache2-tests.yml`](../../.github/workflows/tache2-tests.yml)

Le workflow de tests JUnit de la tâche 2 reste présent. Son étape de tests possède maintenant l'identifiant `tests_tika`. Une nouvelle étape invoque l'action locale `./.github/actions/rickroll` seulement si la commande Maven des tests échoue :

```yaml
- name: Rickroll si les tests Tika echouent
  if: ${{ failure() && steps.tests_tika.outcome == 'failure' }}
  uses: ./.github/actions/rickroll
```

L'action composite écrit une annotation GitHub Actions et ajoute, dans le résumé du job, un lien vers une vidéo Rickroll. Elle n'inverse pas le statut du test : un test en échec reste un échec.

**Validation confirmée :** [exécution GitHub Actions n°35, branche temporaire `verification-rickroll`](https://github.com/rayenrizk/tika/actions/runs/43272682270) (commit `62db6bfc9`). Un test JUnit a été modifié volontairement **uniquement sur cette branche de démonstration** : l'étape `Executer les 31 nouveaux tests` a échoué et l'étape `Rickroll si les tests Tika echouent` s'est exécutée **avec succès**. Le job global reste rouge, ce qui est attendu. En contre-vérification, [l'exécution où les tests réussissent](https://github.com/rayenrizk/tika/actions/runs/43270988537) a ignoré le Rickroll. La branche `verification-rickroll` ne doit pas être fusionnée.

## 6. État, limites et remise

- **Vérifié :** sept types Semgrep distincts, sept nouvelles détections bloquantes, analyse de référence, compilation locale et absence d'erreurs Checkstyle, contrôle GitHub Actions négatif et positif pour Semgrep.
- **Validé également :** scan complet final (2 avant, 9 après, 7 nouvelles alertes) avec artifact GitHub Actions ; Rickroll déclenché sur échec effectif et ignoré lorsque les tests réussissent.
- **À faire :** pull request officielle de remise dans `umontreal-diro/IFT3913`, branche `2026`. Les workflows GitHub indépendants ne sont pas tous nécessairement verts.
- **Limite principale :** les classes ajoutées ne sont pas invoquées par l'application. Le rapport décrit donc honnêtement des vulnérabilités **illustratives**, et non des failles démontrées dans le parcours normal d'utilisation du logiciel.

## 7. Utilisation de l'intelligence artificielle

Une assistance par ChatGPT a été utilisée pour proposer des exemples Java, aider à la configuration des contrôles GitHub Actions, expliquer les messages de Maven et de Semgrep, et préparer ce document. Les résultats rapportés comme validés proviennent des commandes locales et des exécutions GitHub Actions mentionnées ; les résultats non encore vérifiés sont identifiés comme tels. La responsabilité de la vérification et de la remise revient au binôme.

**Sources :** [consignes IFT3913 — tâche 3](https://github.com/umontreal-diro/IFT3913/blob/2026/tache3/readme.md), [Semgrep](https://semgrep.dev/docs/) et [GitHub Actions](https://docs.github.com/en/actions).
