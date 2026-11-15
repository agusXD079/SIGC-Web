# Cómo incorporar este avance a tu repositorio

El asistente no tuvo permiso de escritura sobre `agusXD079/SIGC` (GitHub rechazó crear la rama con HTTP 403), por lo que el avance quedó en una carpeta de trabajo. Estos pasos lo suben a una rama sin tocar `main`.

```bash
git clone https://github.com/agusXD079/SIGC.git
cd SIGC
git checkout -b avance-programacion-2

# 1) Conservar el proyecto de consola original (mantiene su historial)
mkdir -p legacy
git mv TP/concesionaria_TP legacy/consola
# El .dat de la raiz y el de TP son datos locales de prueba; revisarlos antes de decidir si se borran.

# 2) Copiar el avance encima (pom.xml, src/, db/, docs/, .github/, README.md, .gitignore)
#    Si ya tenias README.md, este lo reemplaza: revisalo antes del commit.

# 3) Quitar del indice lo compilado del original, si estaba versionado
git rm -r --cached legacy/consola/bin 2>/dev/null

mvn clean verify          # IMPORTANTE: primera compilacion real
git add -A
git status                # revisar: no debe aparecer ninguna contrasena ni la carpeta fotos/
git commit -m "SIGC: version web (Fases 1 a 4)"
git push -u origin avance-programacion-2
```

Después abrí un Pull Request hacia `main` y fusionalo cuando hayas verificado la ejecución. Nunca subas la contraseña de MySQL: en este proyecto solo existe como variable de entorno.

Si preferís que el asistente publique la rama directamente, hace falta que el conector de GitHub tenga permiso de escritura sobre `agusXD079/SIGC`.
