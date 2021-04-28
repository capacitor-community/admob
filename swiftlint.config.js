module.exports = {
  ...require('@ionic/swiftlint-config'),
  disabled_rules: ['redundant_string_enum_value', 'identifier_name'],
  excluded: [
    'node_modules',
    'ios/Pods',
    'demo'
  ],
};
