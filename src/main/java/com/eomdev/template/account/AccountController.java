package com.eomdev.template.account;

import com.eomdev.template.common.ErrorResponse;
import com.eomdev.template.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

/**
 * Created by eomdev on 2016. 4. 20..
 */
@RestController
@Slf4j
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * 로그인한 유저의 정보
     * @param principal
     * @return 로그인한 유저의 정보, HttpStatus 200
     */
    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public ResponseEntity loginAccount(Principal principal){
        Account userInfo = accountService.findByEmail(principal.getName());
        return new ResponseEntity<>(modelMapper.map(userInfo, AccountDto.Response.class), HttpStatus.OK);
    }

    /**
     * 회원 가입
     * @param create 회원가입을 위한 정보 {@link AccountDto.Create}
     * @param result
     * @return
     */
    @RequestMapping(value = "/account", method = RequestMethod.POST)
    public ResponseEntity createAccount(@RequestBody @Valid AccountDto.Create create,
            BindingResult result) {

        // 입력받은 내용에 에러가 있다면
        if (result.hasErrors()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("잘못된 요청입니다.");
            errorResponse.setCode("bad.request");
            return new ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST);
        }

        Account newAccount = accountService.createAccount(create);
        return new ResponseEntity<>(modelMapper.map(newAccount, AccountDto.Response.class), HttpStatus.CREATED);
    }

    /**
     * 자신 계정 삭제
     * @param principal
     * @return
     */
    @RequestMapping(value = "/account", method = RequestMethod.DELETE)
    public ResponseEntity deleteAccount(Principal principal){
        Account findAccount = accountService.findByEmail(principal.getName());
        accountService.deleteAccount(findAccount.getId());

        return new ResponseEntity<>("정상적으로 삭제되었습니다", HttpStatus.NO_CONTENT);
    }


    /**
     * 계정 정보 수정
     * @param updateDto
     * @param result
     * @return
     */
    @RequestMapping(value = "/account", method = RequestMethod.PUT)
    private ResponseEntity updateAccount(@RequestBody @Valid AccountDto.Update updateDto,
                                         BindingResult result){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        Account account = ((UserDetailsImpl)principal).getAccount();

        if (result.hasErrors()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Account updatedAccount = accountService.updateAccount(account.getId(), updateDto);
        return new ResponseEntity<>(modelMapper.map(updatedAccount, AccountDto.Response.class), HttpStatus.OK);

    }



    /**
     * 회원가입시 중복된 email Exception Handler
     * @param e
     * @return
     */
    @ExceptionHandler(AccountDuplicatedEmailException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUserDuplicatedException(AccountDuplicatedEmailException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("[" + e.getUsername() + "] 중복된 email 주소 입니다.");
        errorResponse.setCode("duplicated.username.exception");
        return errorResponse;
    }

    /**
     * 회원정보 조회시 Account not Found Exception
     * @param e
     * @return
     */
    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleAccountNotFoundException(AccountNotFoundException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("[" + e.getId() + "]에 해당하는 게정이 없습니다.");
        errorResponse.setCode("account.not.found.exception");
        return errorResponse;
    }

}
