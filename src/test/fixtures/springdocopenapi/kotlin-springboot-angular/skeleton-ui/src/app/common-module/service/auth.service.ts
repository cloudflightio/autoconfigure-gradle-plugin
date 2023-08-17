import {Injectable} from '@angular/core';
import {CurrentUserDto, UserService} from '@api';
import {HttpClient} from '@angular/common/http';
import {AuthStore} from '../store/auth.store';
import {firstValueFrom} from 'rxjs';

interface LoginResponse extends CurrentUserDto {
    token: string;
}

@Injectable({
    providedIn: 'root',
})
export class AuthService {
    public constructor(private authStore: AuthStore, private httpClient: HttpClient, private userService: UserService) {}

    public async login(username: string, password: string): Promise<void> {
        this.authStore.reset();
        const loginResponse = await this.authenticate(username, password);
        this.authStore.setCurrentUser(loginResponse);
        this.authStore.setAuthToken(loginResponse.token);
    }

    public async logout(): Promise<void> {
        // TODO: Replace this (+ proxy.config entry) as soon as real authentication is implemented
        // await firstValueFrom(this.httpClient.get('/api/logout'))
        this.authStore.reset();
    }

    public async refreshCurrentUser(): Promise<void> {
        const token = await firstValueFrom(this.authStore.authToken$);
        if (token == null || token === '') {
            throw new Error('Can not refresh, no token set');
        }
        const user = await firstValueFrom(this.userService.getCurrentUser());
        this.authStore.setCurrentUser(user);
    }

    private async authenticate(username: string, password: string): Promise<LoginResponse> {
        // TODO: Replace this (basic-auth) as soon as real authentication is implemented
        const token = btoa(username + ':' + password);
        const response = await firstValueFrom(
            this.httpClient.get<CurrentUserDto>('/api/user', {
                headers: {Authorization: 'Basic ' + token},
            }),
        );
        return {
            ...response,
            token,
        };
    }
}
