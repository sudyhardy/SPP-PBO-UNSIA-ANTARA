FROM mcr.microsoft.com/devcontainers/java:1-17-bookworm

USER root

# Disable the unrelated Yarn repository before installing desktop packages.
RUN for f in /etc/apt/sources.list.d/*; do \
      if [ -f "$f" ] && grep -q 'dl.yarnpkg.com' "$f"; then \
        rm -f "$f"; \
      fi; \
    done
