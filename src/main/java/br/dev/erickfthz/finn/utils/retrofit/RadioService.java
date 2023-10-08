package br.dev.erickfthz.finn.utils.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RadioService {

    @GET("/radios")
    Call<RadioResource> listRadios();
}
