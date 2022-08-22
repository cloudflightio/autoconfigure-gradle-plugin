import Mocked = jest.Mocked;

export function createMock<T>(data: { prototype: T; new (...args: unknown[]): T }): Mocked<T> {
    const keys: (keyof T)[] = (Object.entries(data.prototype) as [keyof T, unknown][])
        .filter(([key, value]) => typeof value === 'function')
        .map(([key, value]) => key);

    return createMockForKeys<T>(keys);
}

export function createMockForKeys<T>(keys: (keyof T)[]): Mocked<T> {
    const obj = {};

    for (const key of keys) {
        Reflect.defineProperty(obj, key, {
            value: jest.fn(),
        });
    }

    return Object.create(obj) as Mocked<T>;
}
