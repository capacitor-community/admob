// @ts-check
const eslint = require("@eslint/js");
const tseslint = require("typescript-eslint");
const angular = require("angular-eslint");
const rdlabo = require('@rdlabo/eslint-plugin-rules');

module.exports = tseslint.config(
  {
    files: ["**/*.ts"],
    plugins: {
      '@rdlabo/rules': rdlabo,
    },
    extends: [
      eslint.configs.recommended,
      ...tseslint.configs.recommended,
      ...tseslint.configs.stylistic,
      ...angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/no-unused-vars": "off",
      "@angular-eslint/directive-selector": "off",
      "@angular-eslint/component-selector": "off",
      "@angular-eslint/no-empty-lifecycle-method": "off",
      "@typescript-eslint/no-empty-function": "off",
      '@rdlabo/rules/deny-constructor-di': 'error',
      '@rdlabo/rules/deny-import-from-ionic-module': 'error',
      '@rdlabo/rules/implements-ionic-lifecycle': 'error',
      '@rdlabo/rules/deny-soft-private-modifier': 'error',
      '@rdlabo/rules/signal-use-as-signal': 'error',
      '@rdlabo/rules/signal-use-as-signal-template': 'error',
      '@rdlabo/rules/component-property-use-readonly': 'error',
    },
  },
  {
    files: ["**/*.html"],
    plugins: {
      '@rdlabo/rules': rdlabo,
    },
    extends: [
      ...angular.configs.templateRecommended,
      ...angular.configs.templateAccessibility,
    ],
    rules: {
      '@rdlabo/rules/ionic-attr-type-check': 'error',
    },
  }
);
