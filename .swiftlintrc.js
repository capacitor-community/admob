const path = require('node:path');
const ionic = require('@ionic/swiftlint-config');

module.exports = {
  ...ionic,
  // node-swiftlint writes this config to a temporary directory; use absolute paths.
  included: ['ios/Sources', 'ios/Tests', 'demo/angular/ios/App/App'].map((dir) => path.join(__dirname, dir)),
  opt_in_rules: ionic.opt_in_rules.filter((rule) => rule !== 'unused_import'),
  analyzer_rules: ['unused_import'],
};
