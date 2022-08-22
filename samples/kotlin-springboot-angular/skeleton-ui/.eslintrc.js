module.exports = {
  root: true,
  env: {
    es6: true,
    node: true,
  },
  parserOptions: {
    project: ['tsconfig.json', 'tsconfig.spec.json'],
    sourceType: 'module',
  },
  settings: {
    'import/resolver': {
      'typescript': {
        'alwaysTryTypes': true,
        'project': [
          'tsconfig.json'
        ]
      }
    }
  },
};
