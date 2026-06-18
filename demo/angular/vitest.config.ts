import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    server: {
      deps: {
        inline: [/@ionic\/angular/, /@ionic\/core/, /ionicons/],
      },
    },
  },
});
