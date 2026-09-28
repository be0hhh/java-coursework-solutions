# JOGL для Linux x86_64

Комплект JOGL / GlueGen **2.5.0**, совместимый с Java 8 (версия class-файлов 52).

Официальные файлы: https://jogamp.org/deployment/v2.5.0/jar/

- `jogl-all.jar`
- `jogl-all-natives-linux-amd64.jar`
- `gluegen-rt.jar`
- `gluegen-rt-natives-linux-amd64.jar`

Все четыре файла должны оставаться в classpath: `-cp 'lib/*'` при компиляции,
`-cp 'build/ИмяКласса:lib/*'` при запуске. Нативные библиотеки извлекаются JOGL автоматически.

`SHA256SUMS` содержит контрольные суммы полученных файлов для проверки неизменности
комплекта: из этой папки выполнить `sha256sum -c SHA256SUMS`.
Эти суммы вычислены локально, а не получены из независимой подписи поставщика.

Документация: https://jogamp.org/wiki/index.php/Setting_up_a_JogAmp_project_in_your_favorite_IDE

В Windows/macOS нужны соответствующие нативные JAR; этот комплект рассчитан на Ubuntu/WSL x86_64.
