import {Injectable} from '@angular/core';
import {CurrentUserDto} from '@api';
import {write} from '@common/util/store-write';
import {createStore, select, withProps} from '@ngneat/elf';

interface AuthStoreState {
    user?: CurrentUserDto;
    token?: string;
}

@Injectable({
    providedIn: 'root',
})
export class AuthStore {
    private readonly store$ = createStore(
        {
            name: 'auth',
        },
        withProps<AuthStoreState>({
            token: localStorage.getItem('token') ?? undefined,
        }),
    );

    public readonly user$ = this.store$.pipe(select((state) => state.user));
    public readonly authToken$ = this.store$.pipe(select((state) => state.token));

    public setCurrentUser(user?: CurrentUserDto): void {
        this.store$.update(
            write((state) => {
                state.user = user;
            }),
        );
    }

    public setAuthToken(token?: string): void {
        if (token != null && token !== '') {
            this.store$.update(
                write((state) => {
                    state.token = token;
                }),
            );
            localStorage.setItem('token', token);
        } else {
            this.store$.update(
                write((state) => {
                    state.token = undefined;
                }),
            );
            localStorage.removeItem('token');
        }
    }

    public reset(): void {
        this.setCurrentUser();
        this.setAuthToken();
    }
}
